package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.AccountServiceEvent;
import de.kaliburg.morefair.account.SuggestionDto;
import de.kaliburg.morefair.api.LadderController;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.services.ChatServiceImpl;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.types.LadderEventTypes;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.chat.*;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The LadderService that setups and manages the LadderEntities contained in a RoundEntity. This
 * Service only handles the matters that regard a specific ladders, like game logic and user input.
 *
 * <p>For global events look at {@link RoundService} or for
 * chats and message at {@link ChatServiceImpl} or {@link MessageService}
 */
@Service
@Log4j2
public class LadderService implements ApplicationListener<AccountServiceEvent> {

  private final RankerService rankerService;
  private final LadderRepository ladderRepository;

  @Getter
  private final CriticalRegion semaphore = new CriticalRegion(1);

  private final AccountService accountService;
  @Getter(AccessLevel.PACKAGE)
  private final Map<Integer, List<Event<LadderEventTypes>>> eventMap = new HashMap<>();
  private final WsUtils wsUtils;
  private final FairConfig config;
  private RoundEntity currentRound;
  @Getter(AccessLevel.PACKAGE)
  private Map<Integer, LadderEntity> currentLadderMap = new HashMap<>();

  public LadderService(RankerService rankerService, LadderRepository ladderRepository,
      AccountService accountService, @Lazy WsUtils wsUtils, FairConfig config) {
    this.rankerService = rankerService;
    this.ladderRepository = ladderRepository;
    this.accountService = accountService;
    this.wsUtils = wsUtils;
    this.config = config;
  }

  @Transactional
  public void saveStateToDatabase(RoundEntity currentRound) {
    try {
      this.currentRound = currentRound;
      int maxNumber = currentLadderMap.values().stream()
          .max(Comparator.comparing(LadderEntity::getNumber)).map(LadderEntity::getNumber)
          .orElse(0);
      for (int i = 1; i <= maxNumber; i++) {
        LadderEntity ladder = currentLadderMap.get(i);
        if (ladder != null) {
          List<RankerEntity> rankerEntities = rankerService.save(ladder.getRankers());
          LadderEntity ladderEntity = ladderRepository.save(ladder);
          ladderEntity.setRankers(rankerEntities);
          currentLadderMap.put(ladder.getNumber(), ladderEntity);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }


  /**
   * Creates a new Ladder for the round of its round with the specific ladderNumber and saves it.
   * <p>
   * If the round is the current Round the ladder will be added to the cache
   * </p>
   *
   * @param round        the round RoundEntity, that this ladder is part of
   * @param ladderNumber the number of the LadderEntity in that round
   * @return the newly created and saved LadderEntity
   */
  public LadderEntity createLadder(RoundEntity round, Integer ladderNumber) {
    LadderEntity result = new LadderEntity(ladderNumber, round);
    result = ladderRepository.save(result);

    round.getLadders().add(result);
    if (currentRound != null && currentRound.getUuid().equals(round.getUuid())) {
      currentLadderMap.put(ladderNumber, result);
      eventMap.put(ladderNumber, new ArrayList<>());
      wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION,
          new Event<>(RoundEventTypes.INCREASE_TOP_LADDER, result.getNumber()));
    }
    return result;
  }

  /**
   * Updates an existing LadderEntity and saves it.
   *
   * @param ladder the ladder that needs to be updated
   * @return the updated and saved ladder
   */
  public LadderEntity updateLadder(LadderEntity ladder) {
    return ladderRepository.save(ladder);
  }

  /**
   * Overwrites the existing cached ladders with the ladders from this round.
   *
   * @param round the round that get their ladders loaded into cache
   */
  public void loadIntoCache(RoundEntity round) {
    currentRound = round;
    Set<LadderEntity> ladders = ladderRepository.findByRound(round);
    if (ladders.isEmpty()) {
      ladders.add(createLadder(round, 1));
    }
    currentLadderMap = new HashMap<>();
    ladders.forEach(ladder -> {
      // TODO: This shouldn't need to be here, i don't know why it pulls multiple instances of
      //  the same ranker sometimes ... my guess is with the way the ladders are handled, there
      //  are 3 ladders for 3 modifier types, that get the rankers and then these get joined into
      //  1 ladder with 3 of the same rankers
      //  - Fix could be to remove the cascade since we only ever pull the rankers here and fetch
      //    the rankers manually
      ladder.setRankers(ladder.getRankers().stream().distinct().collect(Collectors.toList()));
      currentLadderMap.put(ladder.getNumber(), ladder);
      eventMap.put(ladder.getNumber(), new ArrayList<>());
    });
    currentRound.setLadders(ladders);
  }

  /**
   * Finds a ladder based on the uuid. It searches first inside the cache, but if it doesn't find
   * any, it searches the database. If the round of the ladder from the database is the same as the
   * currentRound, it adds the ladder to the database.
   *
   * @param uuid The uuid of the ladder
   * @return the ladder
   */
  public LadderEntity find(UUID uuid) {
    return currentLadderMap.values().stream()
        .filter(ladder -> ladder.getUuid().equals(uuid)).findFirst()
        .orElseGet(() -> {
          LadderEntity result = ladderRepository.findByUuid(uuid).orElse(null);
          if (result != null && result.getRound().getUuid().equals(currentRound.getUuid())) {
            currentLadderMap.put(result.getNumber(), result);
            log.warn("The ladder with the number {} of the current round wasn't found in the cache "
                    + "map but the database, adding it to the cache. (Searched for uuid)",
                result.getNumber());
          }
          return result;
        });
  }

  /**
   * Finds a ladder based on the id. It searches first inside the cache, but if it doesn't find any,
   * it searches the database. If the round of the ladder from the database is the same as the
   * currentRound, it adds the ladder to the database.
   *
   * @param id The id of the ladder
   * @return the ladder
   */
  public LadderEntity find(Long id) {
    return currentLadderMap.values().stream()
        .filter(ladder -> ladder.getId().equals(id)).findFirst()
        .orElseGet(() -> {
          LadderEntity result = ladderRepository.findById(id).orElse(null);
          if (result != null && result.getRound().getUuid().equals(currentRound.getUuid())) {
            log.warn("The ladder with the number {} of the current round wasn't found in the cache "
                    + "map but the database, adding it to the cache. (Searched for id)",
                result.getNumber());
            currentLadderMap.put(result.getNumber(), result);
          }
          return result;
        });
  }

  /**
   * Finds a ladder based on the round and the ladder number. It searches first inside the cache,
   * but if it doesn't find any, it searches the database. If the round of the ladder from the
   * database is the same as the currentRound, it adds the ladder to the database.
   *
   * @param round  The round, that the ladder is part of
   * @param number the number of the ladder in that round
   * @return the ladder
   */
  public LadderEntity find(RoundEntity round, Integer number) {
    LadderEntity result = null;
    boolean isCurrentRound = round.getUuid().equals(getCurrentRound().getUuid());

    if (isCurrentRound) {
      result = currentLadderMap.get(number);
    }

    if (result == null) {
      result = ladderRepository.findByRoundAndNumber(round, number).orElse(null);
      if (result != null && isCurrentRound) {
        log.warn("The ladder with the number {} of the current round wasn't found in the cache "
                + "map but the database, adding it to the cache. (Searched for round+number)",
            result.getNumber());
        currentLadderMap.put(result.getNumber(), result);
      }
    }
    return result;
  }

  public LadderEntity find(LadderEntity ladder) {
    return findInCache(ladder.getNumber());
  }

  /**
   * Finds a ladder based of the ladder number. It searches ONLY inside the cache, since it assumes
   * that the ladder is part of the current round (which should be already cached)
   *
   * @param number the number of the ladder
   * @return the ladder, might be null if there is no ladder with the number
   */
  public LadderEntity findInCache(Integer number) {
    LadderEntity ladder = currentLadderMap.get(number);
    if (ladder == null) {
      log.warn("Couldn't find the ladder with the number {} in cache, checking database.", number);
      ladder = find(currentRound, number);
    }

    return ladder;
  }

  /**
   * Creates a new Ranker on ladder 1.
   *
   * <p>Blocks the ladderSemaphore and eventSemaphore for the duration of the time
   *
   * @param account The Account that gets a new Ranker
   * @return The created Ranker, can be null if one of the acquires gets interrupted
   */
  public RankerEntity createRankerOnLadder(AccountEntity account) {
    try {
      ladderSemaphore.acquire();
      try {
        eventSemaphore.acquire();
        try {
          return createRankerOnLadder(account, 1);
        } finally {
          eventSemaphore.release();
        }
      } finally {
        ladderSemaphore.release();
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates a new Ranker on a ladder.
   *
   * <p>Does NOT block the ladderSemaphore and eventSemaphore; Therefor it should only be used in
   * an internal function where we already block these semaphores
   *
   * @param account The account that gets a new ranker
   * @param number  The number of the ladder that the ranker gets created on
   * @return The created ranker
   */
  RankerEntity createRankerOnLadder(AccountEntity account, Integer number) {
    LadderEntity ladder = findInCache(number);
    account = accountService.find(account);

    if (ladder == null) {
      ladder = createLadder(currentRound, number);
    }

    // Final to be able to use it in a lambda
    final LadderEntity finalLadder = ladder;
    List<RankerEntity> activeRankersInLadder = rankerService.findCurrentActiveRankersOfAccount(
            account, getCurrentRound()).stream()
        .filter(ranker -> ranker.getLadderId().getUuid().equals(finalLadder.getUuid())).toList();

    // Only 1 active ranker per ladder
    if (!activeRankersInLadder.isEmpty()) {
      return activeRankersInLadder.get(0);
    }

    RankerEntity result = rankerService.createNewRanker(account, ladder,
        ladder.getRankers().size() + 1);
    ladder.getRankers().add(result);

    if (ladder.getNumber() == 1) {
      Event<RoundEventTypes> joinEvent = new Event<>(RoundEventTypes.JOIN, account.getId());
      joinEvent.setData(new SuggestionDto(account.getId(), account.getDisplayName()));
      wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION, joinEvent);
    }

    Event<LadderEventTypes> joinEvent = new Event<>(LadderEventTypes.JOIN, account.getId());
    joinEvent.setData(
        new JoinData(account.getDisplayName(), config.getAssholeTag(account.getAssholeCount()),
            account.getAssholePoints()));
    wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION, number,
        joinEvent);

    return result;
  }

  RankerEntity findActiveRankerOfAccountOnLadder(Long accountId, LadderEntity ladder) {
    return find(ladder).getRankers().stream()
        .filter(r -> r.getAccountId().getId().equals(accountId) && r.isGrowing()).findFirst()
        .orElse(null);
  }

  public Optional<RankerEntity> findFirstActiveRankerOfAccountThisRound(AccountEntity account) {
    for (int i = currentRound.getAssholeLadderNumber() + 1; i > 0; i--) {
      LadderEntity ladder = currentLadderMap.get(i);
      if (ladder != null) {
        RankerEntity ranker = findActiveRankerOfAccountOnLadder(account.getId(), ladder);
        if (ranker != null) {
          return Optional.of(ranker);
        }
      }
    }
    return Optional.empty();
  }

  public LadderEntity getHighestLadder() {
    return currentLadderMap.values().stream().max(Comparator.comparing(LadderEntity::getNumber))
        .orElseThrow();
  }


  @Override
  public void onApplicationEvent(AccountServiceEvent event) {
    Map<UUID, AccountEntity> accounts =
        event.getAccounts().stream().collect(Collectors.toMap(AccountEntity::getUuid,
            Function.identity()));

    for (LadderEntity ladder : currentLadderMap.values()) {
      for (RankerEntity ranker : ladder.getRankers()) {
        AccountEntity newAccount = accounts.get(ranker.getAccountId().getUuid());
        if (newAccount != null) {
          ranker.setAccountId(newAccount);
        }
      }
    }
  }
}
