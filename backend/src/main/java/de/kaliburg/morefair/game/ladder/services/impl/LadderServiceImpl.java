package de.kaliburg.morefair.game.ladder.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.chat.services.impl.ChatServiceImpl;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundInitializationService;
import de.kaliburg.morefair.game.round.services.RoundService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
  private final RoundInitializationService roundInitializationService;

  public LadderServiceImpl(LadderRepository ladderRepository, RoundService roundService,
      RoundInitializationService roundInitializationService) {
    this.ladderRepository = ladderRepository;
    this.roundService = roundService;
    this.roundInitializationService = roundInitializationService;

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
      var ladders = ladderRepository.findByRound(currentRound.getId());
      roundInitializationService.validateLadders(currentRound, ladders);
      ladderCache.invalidateAll();
      currentLadderNumberLookup.invalidateAll();
      ladders.forEach(ladder -> {
        ladderCache.put(ladder.getId(), ladder);
        currentLadderNumberLookup.put(ladder.getNumber(), ladder.getId());
      });
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

  }

  @Override
  public Optional<LadderEntity> findLadderById(long ladderId) {
    try (var ignored = semaphore.enter()) {
      return Optional.ofNullable(ladderCache.get(ladderId));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<LadderEntity> findCurrentLadderWithNumber(int ladderNumber) {
    try (var ignored = semaphore.enter()) {
      return Optional.ofNullable(currentLadderNumberLookup.get(ladderNumber))
          .map(ladderCache::get);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<LadderEntity> findAllByRound(RoundEntity round) {
    try (var ignored = semaphore.enter()) {
      return ladderRepository.findByRound(round.getId()).stream()
          .sorted(Comparator.comparingInt(LadderEntity::getNumber))
          .collect(Collectors.toList());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  public LadderEntity createCurrentLadder(int ladderNumber) {
    // TODO: Replace this legacy fallback with explicit VOID recovery before enabling migration.
    throw new UnsupportedOperationException(
        "On-demand ladder generation is disabled; VOID recovery is not implemented (ladder "
            + ladderNumber + ").");
  }


  @Override
  @Transactional
  public LadderEntity save(LadderEntity ladder) {
    LadderEntity result = ladderRepository.save(ladder);
    ladderCache.put(ladder.getId(), result);

    if (roundService.getCurrentRound().getId().equals(result.getRoundId())) {
      currentLadderNumberLookup.put(result.getNumber(), result.getId());
    }

    return result;
  }
}
