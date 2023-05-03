package de.kaliburg.morefair.game.round;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.AccountServiceEvent;
import de.kaliburg.morefair.api.GameController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.data.VinegarData;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.GameResetEvent;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.MessageService;
import de.kaliburg.morefair.statistics.StatisticsService;
import de.kaliburg.morefair.utils.FormattingUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The LadderService that setups and manages the LadderEntities contained in a RoundEntity. This
 * Service only handles the matters that regard a specific ladders, like game logic and user input.
 *
 * <p>For global events look at {@link de.kaliburg.morefair.game.round.RoundService} or for
 * chats and message at {@link de.kaliburg.morefair.game.chat.ChatService} or
 * {@link MessageService}
 */
@Service
@Log4j2
public class LadderService implements ApplicationListener<AccountServiceEvent> {

  private final RankerService rankerService;
  private final LadderRepository ladderRepository;
  @Getter(AccessLevel.PACKAGE)
  private final Semaphore eventSemaphore = new Semaphore(1);
  @Getter(AccessLevel.PACKAGE)
  private final Semaphore ladderSemaphore = new Semaphore(1);
  private final LadderUtils ladderUtils;
  private final AccountService accountService;
  private final StatisticsService statisticsService;
  private final RoundUtils roundUtils;
  private final ChatService chatService;
  private final UpgradeUtils upgradeUtils;
  private final ApplicationEventPublisher eventPublisher;
  @Getter(AccessLevel.PACKAGE)
  private final Map<Integer, List<Event>> eventMap = new HashMap<>();
  private final WsUtils wsUtils;
  private final FairConfig config;
  private final Gson gson;
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private RoundEntity currentRound;
  @Getter(AccessLevel.PACKAGE)
  private Map<Integer, LadderEntity> currentLadderMap = new HashMap<>();

  public LadderService(RankerService rankerService, LadderRepository ladderRepository,
      LadderUtils ladderUtils, AccountService accountService,
      @Lazy StatisticsService statisticsService,
      RoundUtils roundUtils,
      ChatService chatService, UpgradeUtils upgradeUtils, ApplicationEventPublisher eventPublisher,
      @Lazy WsUtils wsUtils, FairConfig config, Gson gson) {
    this.rankerService = rankerService;
    this.ladderRepository = ladderRepository;
    this.ladderUtils = ladderUtils;
    this.accountService = accountService;
    this.statisticsService = statisticsService;
    this.roundUtils = roundUtils;
    this.chatService = chatService;
    this.upgradeUtils = upgradeUtils;
    this.eventPublisher = eventPublisher;
    this.wsUtils = wsUtils;
    this.config = config;
    this.gson = gson;
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
   * Adds an event to the list of events inside the eventMap. This calls a semaphore and should
   * thereby only be done by the Controllers/API.
   *
   * @param event the event that gets added to the eventMap
   */
  public void addEvent(@NonNull Integer ladderNumber, Event event) {
    try {
      eventSemaphore.acquire();
      try {
        eventMap.get(ladderNumber).add(event);
      } finally {
        eventSemaphore.release();
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
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
  RankerEntity createRanker(AccountEntity account) {
    try {
      ladderSemaphore.acquire();
      try {
        eventSemaphore.acquire();
        try {
          return createRanker(account, 1);
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
  RankerEntity createRanker(AccountEntity account, Integer number) {
    LadderEntity ladder = findInCache(number);
    account = accountService.find(account);

    if (ladder == null) {
      ladder = createLadder(currentRound, number);
    }

    // Final to be able to use it in a lambda
    final LadderEntity finalLadder = ladder;
    List<RankerEntity> activeRankersInLadder = rankerService.findCurrentActiveRankersOfAccount(
            account, getCurrentRound()).stream()
        .filter(ranker -> ranker.getLadder().getUuid().equals(finalLadder.getUuid())).toList();

    // Only 1 active ranker per ladder
    if (!activeRankersInLadder.isEmpty()) {
      return activeRankersInLadder.get(0);
    }

    RankerEntity result = rankerService.create(account, ladder,
        ladder.getRankers().size() + 1);
    ladder.getRankers().add(result);

    Event joinEvent = new Event(EventType.JOIN, account.getId());
    joinEvent.setData(
        new JoinData(account.getUsername(), config.getAssholeTag(account.getAssholeCount()),
            account.getAssholePoints()));
    wsUtils.convertAndSendToTopic(GameController.TOPIC_EVENTS_DESTINATION.replace("{number}",
        ladder.getNumber().toString()), joinEvent);

    return result;
  }

  RankerEntity findActiveRankerOfAccountOnLadder(Long accountId, LadderEntity ladder) {
    return find(ladder).getRankers().stream()
        .filter(r -> r.getAccount().getId().equals(accountId) && r.isGrowing()).findFirst()
        .orElse(null);
  }

  public RankerEntity findFirstActiveRankerOfAccountThisRound(AccountEntity account) {
    for (int i = currentRound.getAssholeLadderNumber() + 1; i > 0; i--) {
      LadderEntity ladder = currentLadderMap.get(i);
      if (ladder != null) {
        RankerEntity ranker = findActiveRankerOfAccountOnLadder(account.getId(), ladder);
        if (ranker != null) {
          return ranker;
        }
      }
    }
    return null;
  }

  public LadderEntity getHighestLadder() {
    return currentLadderMap.values().stream().max(Comparator.comparing(LadderEntity::getNumber))
        .orElseThrow();
  }

  /**
   * Buy Bias for the active ranker of an account on a specific ladder.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can buy bias
   */
  boolean buyBias(Event event, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(event.getAccountId(), ladder);
      BigInteger cost = upgradeUtils.buyUpgradeCost(ladder.getNumber(), ranker.getBias(),
          ladder.getTypes());
      if (ranker.getPoints().compareTo(cost) >= 0) {
        statisticsService.recordBias(ranker, ladder, currentRound);
        ranker.setPoints(BigInteger.ZERO);
        ranker.setBias(ranker.getBias() + 1);
        wsUtils.convertAndSendToTopic(GameController.TOPIC_EVENTS_DESTINATION.replace("{number}",
            ladder.getNumber().toString()), event);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Buy multi for the active ranker of an account on a specific ladder.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can buy multi
   */
  boolean buyMulti(Event event, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(event.getAccountId(), ladder);
      BigInteger cost = upgradeUtils.buyUpgradeCost(ladder.getNumber(), ranker.getMultiplier(),
          ladder.getTypes());
      if (ranker.getPower().compareTo(cost) >= 0) {
        statisticsService.recordMulti(ranker, ladder, currentRound);
        ranker.setPoints(BigInteger.ZERO);
        ranker.setPower(BigInteger.ZERO);
        ranker.setBias(0);
        ranker.setMultiplier(ranker.getMultiplier() + 1);
        wsUtils.convertAndSendToTopic(GameController.TOPIC_EVENTS_DESTINATION.replace("{number}",
            ladder.getNumber().toString()), event);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Buy auto-promote for the active ranker of an account on a specific ladder.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can buy auto-promote
   */
  boolean buyAutoPromote(Event event, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(event.getAccountId(), ladder);
      if (ranker == null) {
        return false;
      }

      BigInteger cost = upgradeUtils.buyAutoPromoteCost(ranker.getRank(), ladder.getNumber());

      if (ladder.getTypes().contains(LadderType.FREE_AUTO)) {
        ranker.setAutoPromote(true);
        wsUtils.convertAndSendToUser(ranker.getAccount().getUuid(),
            GameController.PRIVATE_EVENTS_DESTINATION, event);
        return true;
      }

      if (ladderUtils.canBuyAutoPromote(ladder, ranker, currentRound)) {
        statisticsService.recordAutoPromote(ranker, ladder, currentRound);
        ranker.setGrapes(ranker.getGrapes().subtract(cost));
        ranker.setAutoPromote(true);
        wsUtils.convertAndSendToUser(ranker.getAccount().getUuid(),
            GameController.PRIVATE_EVENTS_DESTINATION, event);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Promote the active ranker of an account on a specific ladder.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can promote
   */
  boolean promote(Event event, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(event.getAccountId(), ladder);
      if (ladderUtils.canPromote(ladder, ranker)) {
        statisticsService.recordPromote(ranker, ladder, currentRound);
        AccountEntity account = accountService.find(ranker.getAccount());
        log.info("[L{}] Promotion for {} (#{})", ladder.getNumber(), account.getUsername(),
            account.getId());
        ranker.setGrowing(false);

        RankerEntity newRanker = createRanker(account, ladder.getNumber() + 1);
        newRanker.setVinegar(ranker.getVinegar());
        newRanker.setGrapes(ranker.getGrapes());
        newRanker.getUnlocks().copy(ranker.getUnlocks());
        LadderEntity newLadder = findInCache(newRanker.getLadder().getNumber());

        // Auto-Ladder
        Integer number = Math.floorDiv(newLadder.getNumber(), 2) - 2;
        LadderEntity autoLadder = findInCache(number);
        if (autoLadder != null && !autoLadder.getTypes().contains(LadderType.FREE_AUTO)
            && !autoLadder.getTypes().contains(LadderType.NO_AUTO)) {
          autoLadder.getTypes().add(LadderType.FREE_AUTO);
        }

        // Unlocks
        if (!newRanker.getUnlocks().getAutoPromote()
            && newLadder.getNumber() >= config.getAutoPromoteLadder()) {
          newRanker.getUnlocks().setAutoPromote(true);
        }
        if (!newRanker.getUnlocks().getReachedBaseAssholeLadder()
            && newLadder.getNumber() >= currentRound.getModifiedBaseAssholeLadder()) {
          newRanker.getUnlocks().setReachedBaseAssholeLadder(true);
        }
        if (!newRanker.getUnlocks().getReachedAssholeLadder()
            && newLadder.getNumber() >= currentRound.getAssholeLadderNumber()) {
          newRanker.getUnlocks().setReachedAssholeLadder(true);
        }
        if (!newRanker.getUnlocks().getPressedAssholeButton()
            && ladder.getTypes().contains(LadderType.ASSHOLE)) {
          newRanker.getUnlocks().setPressedAssholeButton(true);
          account.getAchievements().setPressedAssholeButton(true);
        }

        // Rewards for finishing first / at the top
        if (newLadder.getRankers().size() <= 1) {
          newRanker.setAutoPromote(true);
          newRanker.setVinegar(
              newRanker.getVinegar().multiply(BigInteger.valueOf(12)).divide(BigInteger.TEN));
        }

        BigInteger autoPromoteCost = config.getBaseGrapesToBuyAutoPromote();

        if (newLadder.getRankers().size() <= 3) {
          newRanker.setGrapes(newRanker.getGrapes().add(autoPromoteCost));
        } else if (newLadder.getRankers().size() <= 5) {
          newRanker.setGrapes(newRanker.getGrapes().add(autoPromoteCost.divide(BigInteger.TWO)));
        } else if (newLadder.getRankers().size() <= currentRound.getBaseAssholeLadder()) {
          newRanker.setGrapes(newRanker.getGrapes().add(autoPromoteCost.divide(BigInteger.TEN)));
        }

        // Create new Chat if it doesn't exist
        if (chatService.find(ladder.getNumber() + 1) == null) {
          chatService.create(ladder.getNumber() + 1);
        }

        wsUtils.convertAndSendToTopic(GameController.TOPIC_EVENTS_DESTINATION.replace("{number}",
            ladder.getNumber().toString()), event);
        account = accountService.save(account);

        // Logic for the Asshole-Ladder
        if (newRanker.getUnlocks().getPressedAssholeButton()) {
          JsonObject object1 = new JsonObject();
          object1.addProperty("u", account.getUsername());
          object1.addProperty("id", account.getId());
          object1.addProperty("i", 0);

          AccountEntity broadCaster = accountService.findBroadcaster();
          JsonObject object2 = new JsonObject();
          object2.addProperty("u", broadCaster.getUsername());
          object2.addProperty("id", broadCaster.getId());
          object2.addProperty("i", 20);

          String metadataString = gson.toJson(new JsonObject[]{object1, object2});

          chatService.sendGlobalMessage("{@} was welcomed by {@}. They are the "
              + FormattingUtils.ordinal(newLadder.getRankers().size())
              + " lucky initiate for the " + FormattingUtils.ordinal(
              currentRound.getNumber()) + " big ritual.", metadataString);

          int neededAssholesForReset = currentRound.getAssholesForReset();
          int assholeCount = newLadder.getRankers().size();

          // Is it time to reset the game
          if (assholeCount >= neededAssholesForReset) {
            LadderEntity firstLadder = findInCache(1);
            List<AccountEntity> accounts =
                firstLadder.getRankers().stream().map(RankerEntity::getAccount).distinct().toList();
            for (AccountEntity entity : accounts) {
              RankerEntity highestRanker = findFirstActiveRankerOfAccountThisRound(entity);
              UnlocksEntity unlocks = highestRanker.getUnlocks();
              entity.setAssholePoints(entity.getAssholePoints() + unlocks.calculateAssholePoints());
            }

            accountService.save(accounts);
            saveStateToDatabase(currentRound);
            eventPublisher.publishEvent(new GameResetEvent(this));
            wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION,
                new Event(EventType.RESET, account.getId()));
          }
        }
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Throw vinegar as the active ranker of an account on a specific ladder.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can throw vinegar at the rank-1-ranker
   */
  boolean throwVinegar(Event event, LadderEntity ladder) {
    try {
      ladder = find(ladder);
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(event.getAccountId(), ladder);
      RankerEntity target = ladder.getRankers().get(0);
      AccountEntity rankerAccount = accountService.find(ranker.getAccount());
      AccountEntity targetAccount = accountService.find(target.getAccount());

      if (target.isAutoPromote() || ladder.getTypes().contains(LadderType.FREE_AUTO)) {
        log.info("[L{}] {} (#{}) tried to throw Vinegar at {} (#{}), but they had Auto-Promote!",
            ladder.getNumber(), rankerAccount.getUsername(), rankerAccount.getId(),
            targetAccount.getUsername(), targetAccount.getId());
        return false;
      }

      if (ladderUtils.canThrowVinegarAt(ladder, ranker, target)) {
        statisticsService.recordVinegarThrow(ranker, target, ladder, currentRound);
        BigInteger rankerVinegar = ranker.getVinegar();
        BigInteger targetVinegar = target.getVinegar();

        log.info("[L{}] {} (#{}) is using their {} Vinegar on {} (#{}) with {} Vinegar",
            ladder.getNumber(), rankerAccount.getUsername(), rankerAccount.getId(), rankerVinegar,
            targetAccount.getUsername(), targetAccount.getId(), targetVinegar);

        VinegarData data = new VinegarData(rankerVinegar.toString(), targetAccount.getId());
        if (targetVinegar.compareTo(rankerVinegar) > 0) {
          targetVinegar = targetVinegar.subtract(rankerVinegar);
        } else {
          targetVinegar = BigInteger.ZERO;
          data.setSuccess(true);
        }

        event.setData(data);
        wsUtils.convertAndSendToUser(ranker.getAccount().getUuid(),
            GameController.PRIVATE_EVENTS_DESTINATION, event);
        wsUtils.convertAndSendToUser(target.getAccount().getUuid(),
            GameController.PRIVATE_EVENTS_DESTINATION, event);

        if (data.isSuccess()) {
          if (!buyMulti(new Event(EventType.BUY_MULTI, targetAccount.getId()), ladder)) {
            softResetPoints(new Event(EventType.SOFT_RESET_POINTS, targetAccount.getId()),
                ladder);
          }
        }

        ranker.setVinegar(BigInteger.ZERO);
        target.setVinegar(targetVinegar);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Soft-reset the points of the active ranker of an account on a specific ladder. This mainly
   * happens after successfully thrown vinegar.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can be soft-reset
   */
  boolean softResetPoints(Event event, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(event.getAccountId(), ladder);
      ranker.setPoints(BigInteger.ZERO);
      ranker.setPower(ranker.getPower().divide(BigInteger.TWO));
      wsUtils.convertAndSendToTopic(GameController.TOPIC_EVENTS_DESTINATION.replace("{number}",
          ladder.getNumber().toString()), event);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public void onApplicationEvent(AccountServiceEvent event) {
    Map<UUID, AccountEntity> accounts =
        event.getAccounts().stream().collect(Collectors.toMap(AccountEntity::getUuid,
            Function.identity()));

    for (LadderEntity ladder : currentLadderMap.values()) {
      for (RankerEntity ranker : ladder.getRankers()) {
        AccountEntity newAccount = accounts.get(ranker.getAccount().getUuid());
        if (newAccount != null) {
          ranker.setAccount(newAccount);
        }
      }
    }
  }
}
