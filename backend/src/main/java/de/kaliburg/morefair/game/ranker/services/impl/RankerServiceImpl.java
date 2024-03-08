package de.kaliburg.morefair.game.ranker.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.api.LadderController;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.model.dto.SuggestionDto;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.repositories.RankerRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The RankerService that uses a Loading Cache to cache the Lists of Rankers.
 */
@Service
@Log4j2
public class RankerServiceImpl implements RankerService {

  private final LoadingCache<Long, List<RankerEntity>> rankerCache;
  private final CriticalRegion semaphore = new CriticalRegion(1);
  private final RankerRepository rankerRepository;
  private final LadderService ladderService;
  private final RoundService roundService;
  private final WsUtils wsUtils;
  private final FairConfig fairConfig;

  /**
   * Default Constructor.
   */
  public RankerServiceImpl(RankerRepository rankerRepository, LadderService ladderService,
      RoundService roundService, WsUtils wsUtils, FairConfig fairConfig) {
    this.rankerCache = Caffeine.newBuilder()
        .build(rankerRepository::findByLadderId);

    this.rankerRepository = rankerRepository;
    this.ladderService = ladderService;
    this.roundService = roundService;
    this.wsUtils = wsUtils;
    this.fairConfig = fairConfig;
  }


  @Override
  public void enterNewRanker(AccountEntity account) {
    createRankerOnLadder(account, 1);
  }

  @Override
  public List<RankerEntity> findAllByLadderId(Long ladderId) {
    // Semaphore start here
    try (var ignored = semaphore.enter()) {
      return ladderService.findLadderById(ladderId)
          .map(l -> rankerCache.get(l.getId()))
          .orElse(List.of());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<RankerEntity> findAllByCurrentLadderNumber(int ladderNumber) {
    // Semaphore start here
    try (var ignored = semaphore.enter()) {
      return ladderService.findCurrentLadderWithNumber(ladderNumber)
          .map(l -> rankerCache.get(l.getId()))
          .orElse(List.of());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Finds the highest Ranker in the current Set of Ladders, that is being owned by the account.
   *
   * <p>This is a high performance function, which is basically called in every interaction the
   * player has with the Server. <p/>TODO: Use a LoadingCache for AccountId -> RankerEntity
   *
   * @param account The account, that the highest Ranker is searched of.
   * @return The highest Ranker of the account, if any exists.
   */
  @Override
  public Optional<RankerEntity> findHighestActiveRankerOfAccount(AccountEntity account) {
    try (var ignored = semaphore.enter()) {
      RoundEntity currentRound = roundService.getCurrentRound();

      List<Long> allLadderIdsInCurrentRound = ladderService.findAllByRound(currentRound).stream()
          .map(LadderEntity::getId)
          .toList();

      return rankerCache.getAll(allLadderIdsInCurrentRound).values().stream()
          // map each ladder to the ranker, owned by the account
          .map(
              list -> list.stream()
                  .filter(RankerEntity::isGrowing)
                  .filter(r -> r.getAccountId().equals(account.getId()))
                  .findAny().orElse(null)
          )
          // filter only the ladders that actually have a ranker of that account
          .filter(Objects::nonNull)
          // finding the highest Ranker in these Rounds
          .max(Comparator.comparing(
              r -> ladderService.findLadderById(r.getLadderId())
                  .map(LadderEntity::getNumber)
                  .orElseThrow()
          ));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<RankerEntity> createRankerOnLadder(AccountEntity account, int ladderNumber) {
    try (var ignored = semaphore.enter()) {
      LadderEntity ladder = ladderService.findCurrentLadderWithNumber(ladderNumber)
          .orElse(ladderService.createCurrentLadder(ladderNumber));

      List<RankerEntity> rankers = rankerCache.get(ladder.getId());
      Optional<RankerEntity> optionalRanker = rankers.stream()
          .filter(r -> r.getAccountId().equals(account.getId()))
          .findAny();

      // Only 1 active ranker per ladder
      if (optionalRanker.isPresent()) {
        return Optional.empty();
      }

      RankerEntity result = create(account, ladder, rankers.size() + 1);
      rankers.add(result);
      rankerCache.put(ladder.getId(), rankers);

      if (ladder.getNumber() == 1) {
        Event<RoundEventTypes> joinEvent = new Event<>(RoundEventTypes.JOIN, account.getId());
        joinEvent.setData(new SuggestionDto(account.getId(), account.getDisplayName()));
        wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION, joinEvent);

        // FIXME: roundService -> update Stats
      }

      Event<LadderEventType> joinEvent = new Event<>(LadderEventType.JOIN, account.getId());
      joinEvent.setData(
          new JoinData(account.getDisplayName(),
              fairConfig.getAssholeTag(account.getAssholeCount()),
              account.getAssholePoints()));
      wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
          ladderNumber,
          joinEvent);

      return Optional.of(result);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private RankerEntity create(AccountEntity account, LadderEntity ladder, Integer rank) {
    RankerEntity result = RankerEntity.builder()
        .ladderId(ladder.getId())
        .accountId(account.getId())
        .rank(rank)
        .build();

    return rankerRepository.save(result);
  }
}
