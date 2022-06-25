package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.data.VinegarData;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.MessageService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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
public class LadderService {

  private final RankerService rankerService;
  private final LadderRepository ladderRepository;
  @Getter(AccessLevel.PACKAGE)
  private final Semaphore eventSemaphore = new Semaphore(1);
  @Getter(AccessLevel.PACKAGE)
  private final Semaphore ladderSemaphore = new Semaphore(1);
  private final LadderUtils ladderUtils;
  private final AccountService accountService;
  private final RoundUtils roundUtils;
  private final Random randomGenerator = new Random();
  private final ChatService chatService;
  private final UpgradeUtils upgradeUtils;
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private RoundEntity currentRound;
  @Getter(AccessLevel.PACKAGE)
  private Map<Integer, LadderEntity> currentLadderMap = new HashMap<>();
  @Getter(AccessLevel.PACKAGE)
  private Map<Integer, List<Event>> eventMap = new HashMap<>();

  public LadderService(RankerService rankerService, LadderRepository ladderRepository,
      LadderUtils ladderUtils, AccountService accountService, RoundUtils roundUtils,
      ChatService chatService, UpgradeUtils upgradeUtils) {
    this.rankerService = rankerService;
    this.ladderRepository = ladderRepository;
    this.ladderUtils = ladderUtils;
    this.accountService = accountService;
    this.roundUtils = roundUtils;
    this.chatService = chatService;
    this.upgradeUtils = upgradeUtils;
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
    if (currentRound != null && currentRound.getUuid().equals(round.getUuid())) {
      currentLadderMap.put(ladderNumber, result);
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
    currentLadderMap = new HashMap<>();
    List<LadderEntity> ladders = ladderRepository.findByRoundOrderByNumberAsc(round);

    ladders.forEach(ladder -> {
      currentLadderMap.put(ladder.getNumber(), ladder);
      eventMap.put(ladder.getNumber(), new ArrayList<>());
    });
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
          LadderEntity result = ladderRepository.findByUuid(uuid).orElseThrow();
          if (result.getRound().getUuid().equals(currentRound.getUuid())) {
            currentLadderMap.put(result.getNumber(), result);
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
          LadderEntity result = ladderRepository.findById(id).orElseThrow();
          if (result.getRound().getUuid().equals(currentRound.getUuid())) {
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
    return currentLadderMap.values().stream()
        .filter(ladder -> ladder.getRound().equals(round) && ladder.getNumber().equals(number))
        .findFirst()
        .orElseGet(() -> {
          LadderEntity result = ladderRepository.findByRoundAndNumber(round, number).orElseThrow();
          if (result.getRound().getUuid().equals(currentRound.getUuid())) {
            currentLadderMap.put(result.getNumber(), result);
          }
          return result;
        });
  }

  public LadderEntity find(LadderEntity ladder) {
    return find(ladder.getId());
  }

  /**
   * Finds a ladder based of the ladder number. It searches ONLY inside the cache, since it assumes
   * that the ladder is part of the current round (which should be already cached)
   *
   * @param number the number of the ladder
   * @return the ladder, might be null if there is no ladder with the number
   */
  public LadderEntity find(Integer number) {
    LadderEntity ladder = currentLadderMap.get(number);
    if (ladder == null) {
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
    LadderEntity ladder = find(number);

    if (ladder == null) {
      ladder = createLadder(currentRound, number);
    }

    final LadderEntity finalLadder = ladder;
    List<RankerEntity> activeRankersInLadder = account.getActiveRankers().stream()
        .filter(ranker -> ranker.getLadder().getUuid().equals(finalLadder.getUuid())).toList();

    // Only 1 active ranker per ladder
    if (!activeRankersInLadder.isEmpty()) {
      return activeRankersInLadder.get(0);
    }

    RankerEntity result = rankerService.create(account, ladder, ladder.getRankers().size() + 1);
    ladder.getRankers().add(result);

    Event joinEvent = new Event(EventType.JOIN, account.getId());
    joinEvent.setData(new JoinData(account.getUsername(), account.getAssholeCount()));
    eventMap.get(number).add(joinEvent);

    return result;
  }

  RankerEntity findActiveRankerOfAccountOnLadder(Long accountId, LadderEntity ladder) {
    return find(ladder).getRankers().stream()
        .filter(r -> r.getAccount().getId().equals(accountId) && r.isGrowing()).findFirst()
        .orElseThrow();
  }


  public LadderEntity getHighestLadder() {
    return currentLadderMap.values().stream().max(Comparator.comparing(LadderEntity::getNumber))
        .orElseThrow();
  }

  /**
   * Buy Bias for the active ranker of an account on a specific ladder.
   *
   * @param accountId the id of the account
   * @param ladder    the ladder the ranker is on
   * @return if the ranker can buy bias
   */
  boolean buyBias(Long accountId, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
      BigInteger cost = upgradeUtils.buyUpgradeCost(ladder.getNumber(), ranker.getBias());
      if (ranker.getPoints().compareTo(cost) >= 0) {
        ranker.setPoints(BigInteger.ZERO);
        ranker.setBias(ranker.getBias() + 1);
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
   * @param accountId the id of the account
   * @param ladder    the ladder the ranker is on
   * @return if the ranker can buy multi
   */
  boolean buyMulti(Long accountId, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
      BigInteger cost = upgradeUtils.buyUpgradeCost(ladder.getNumber(), ranker.getMultiplier());
      if (ranker.getPower().compareTo(cost) >= 0) {
        ranker.setPoints(BigInteger.ZERO);
        ranker.setPower(BigInteger.ZERO);
        ranker.setBias(0);
        ranker.setMultiplier(ranker.getMultiplier() + 1);
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
   * @param accountId the id of the account
   * @param ladder    the ladder the ranker is on
   * @return if the ranker can buy auto-promote
   */
  boolean buyAutoPromote(Long accountId, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
      BigInteger cost = upgradeUtils.buyAutoPromoteCost(ranker.getRank(), ladder.getNumber());

      if (ladderUtils.canBuyAutoPromote(ladder, ranker, currentRound)) {
        ranker.setGrapes(ranker.getGrapes().subtract(cost));
        ranker.setAutoPromote(true);
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
   * @param accountId the id of the account
   * @param ladder    the ladder the ranker is on
   * @return if the ranker can promote
   */
  boolean promote(Long accountId, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
      if (ladderUtils.canPromote(ladder, ranker)) {
        AccountEntity account = accountService.find(ranker.getAccount());
        log.info("[L{}] Promotion for {} (#{})", ladder.getRankers(), account.getUsername(),
            account.getId());
        ranker.setGrowing(false);
        ranker = rankerService.save(ranker);
        RankerEntity newRanker = createRanker(account, ladder.getNumber() + 1);
        newRanker.setVinegar(ranker.getVinegar());
        newRanker.setGrapes(ranker.getGrapes());
        LadderEntity newLadder = find(newRanker.getLadder());

        if (newLadder.getRankers().size() == 1) {
          newRanker.setAutoPromote(true);
        }

        account = accountService.save(accountService.find(account));
        if (newLadder.getNumber() > roundUtils.getAssholeLadderNumber(currentRound)) {
          chatService.sendGlobalMessage(account,
              account.getUsername() + " was welcomed by Chad. They are number "
                  + newLadder.getRankers().size()
                  + " of the lucky few initiates for the big ritual.");
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
   * @param accountId the id of the account
   * @param ladder    the ladder the ranker is on
   * @return if the ranker can throw vinegar at the rank-1-ranker
   */
  boolean throwVinegar(Long accountId, LadderEntity ladder, Event event) {
    try {
      ladder = find(ladder);
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
      RankerEntity target = ladder.getRankers().get(0);
      AccountEntity rankerAccount = accountService.find(ranker.getAccount());
      AccountEntity targetAccount = accountService.find(target.getAccount());

      if (target.isAutoPromote()) {
        log.info("[L{}] {} (#{}) tried to throw Vinegar at {} (#{}), but they had Auto-Promote!",
            ladder.getNumber(), rankerAccount.getUsername(), rankerAccount.getId(),
            targetAccount.getUsername(), targetAccount.getId());
        return false;
      }

      if (ladderUtils.canThrowVinegarAt(ladder, ranker, target)) {
        BigInteger rankerVinegar = ranker.getVinegar();
        BigInteger targetVinegar = target.getVinegar();

        log.info("[L{}] {} (#{}) is using their {} Vinegar on {} (#{}) with {} Vinegar",
            ladder.getNumber(), rankerAccount.getUsername(), rankerAccount.getId(), rankerVinegar,
            targetAccount.getUsername(), targetAccount.getId(), targetVinegar);

        VinegarData data = new VinegarData(rankerVinegar.toString());
        if (targetVinegar.compareTo(rankerVinegar) > 0) {
          targetVinegar = targetVinegar.subtract(rankerVinegar);
        } else {
          targetVinegar = BigInteger.ZERO;
          data.setSuccess(true);
          if (!buyMulti(targetAccount.getId(), ladder)) {
            softResetPoints(targetAccount.getId(), ladder);
          }
        }

        event.setData(data);
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
   * Soft-reset the points of the active ranker of an account on a specific ladder.
   *
   * @param accountId the id of the account
   * @param ladder    the ladder the ranker is on
   * @return if the ranker can be soft-reset
   */
  boolean softResetPoints(Long accountId, LadderEntity ladder) {
    try {
      RankerEntity ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
      ranker.setPoints(BigInteger.ZERO);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }

    return false;
  }


}
