package de.kaliburg.morefair.game.ladder.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.chat.services.impl.ChatServiceImpl;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The LadderService that setups and manages the LadderEntities contained in a Round. This Service
 * only handles the matters like fetching/caching ladders.
 *
 * <p>For global events look at {@link RoundService} or for
 * chats and message at {@link ChatServiceImpl} or {@link MessageService}
 */
@Service
@Log4j2
public class LadderServiceImpl implements LadderService {

  private final CriticalRegion semaphore = new CriticalRegion(1);
  private final LoadingCache<Long, LadderEntity> ladderCache;
  private final LoadingCache<Integer, Long> currentLadderNumberLookup;
  private final LadderRepository ladderRepository;
  private final RoundService roundService;
  private final WsUtils wsUtils;

  public LadderServiceImpl(LadderRepository ladderRepository, RoundService roundService,
      WsUtils wsUtils) {
    this.ladderRepository = ladderRepository;
    this.roundService = roundService;
    this.wsUtils = wsUtils;

    this.ladderCache = Caffeine.newBuilder()
        .expireAfterAccess(Duration.of(10, ChronoUnit.MINUTES))
        .build(id -> this.ladderRepository.findById(id).orElse(null));

    this.currentLadderNumberLookup = Caffeine.newBuilder()
        .expireAfterAccess(Duration.of(10, ChronoUnit.MINUTES))
        .build(
            number -> this.ladderRepository.findByRoundAndNumber(
                    roundService.getCurrentRound().getId(),
                    number)
                .map(LadderEntity::getId)
                .orElse(null)
        );
  }

  @PostConstruct
  public void init() {
    reloadLadders();
  }

  @Override
  public void reloadLadders() {
    try (var ignored = semaphore.enter()) {
      RoundEntity currentRound = roundService.getCurrentRound();
      currentLadderNumberLookup.invalidateAll();
      Set<Integer> currentLadders = ladderRepository.findByRound(currentRound.getId()).stream()
          .map(LadderEntity::getNumber)
          .collect(Collectors.toSet());

      currentLadderNumberLookup.getAll(currentLadders);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public Optional<LadderEntity> findLadderById(long ladderId) {
    try (var ignored = semaphore.enter()) {
      return Optional.ofNullable(ladderCache.get(ladderId));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<LadderEntity> findCurrentLadderWithNumber(int ladderNumber) {
    try (var ignored = semaphore.enter()) {
      return Optional.ofNullable(currentLadderNumberLookup.get(ladderNumber))
          .map(ladderCache::get);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<LadderEntity> findAllByRound(RoundEntity round) {
    try (var ignored = semaphore.enter()) {
      if (roundService.getCurrentRound().getId().equals(round.getId())) {
        Collection<Long> ids = currentLadderNumberLookup.asMap().values();
        Collection<LadderEntity> all = ladderCache.getAll(ids).values();

        return all.stream()
            .sorted(Comparator.comparingInt(LadderEntity::getNumber))
            .collect(Collectors.toList());
      }

      // The Reason we don't cache this, is because it's not the stuff of the current Round
      return ladderRepository.findByRound(round.getId()).stream().toList();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional
  public LadderEntity createCurrentLadder(int ladderNumber) {
    try (var ignored = semaphore.enter()) {
      if (currentLadderNumberLookup.get(ladderNumber) != null) {
        return null;
      }

      LadderEntity previousLadder =
          Optional.ofNullable(currentLadderNumberLookup.get(ladderNumber - 1))
              .map(ladderCache::get)
              .orElse(null);

      LadderEntity result =
          new LadderEntity(ladderNumber, roundService.getCurrentRound(), previousLadder);
      result = ladderRepository.save(result);

      Event<RoundEventTypes> topLadderEvent = new Event<>(RoundEventTypes.INCREASE_TOP_LADDER);
      topLadderEvent.setData(ladderNumber);
      wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION, topLadderEvent);

      currentLadderNumberLookup.put(result.getNumber(), result.getId());
      ladderCache.put(result.getId(), result);
      return result;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


}
