package de.kaliburg.morefair.game.ladder.services;

import static de.kaliburg.morefair.events.types.LadderEventTypes.BUY_AUTO_PROMOTE;
import static de.kaliburg.morefair.events.types.LadderEventTypes.BUY_BIAS;
import static de.kaliburg.morefair.events.types.LadderEventTypes.BUY_MULTI;
import static de.kaliburg.morefair.events.types.LadderEventTypes.PROMOTE;
import static de.kaliburg.morefair.events.types.LadderEventTypes.THROW_VINEGAR;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.AccountController;
import de.kaliburg.morefair.api.FairController;
import de.kaliburg.morefair.api.LadderController;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.ChatType;
import de.kaliburg.morefair.chat.services.ChatService;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.VinegarData;
import de.kaliburg.morefair.events.types.AccountEventTypes;
import de.kaliburg.morefair.events.types.LadderEventTypes;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.UnlocksEntity;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.ladder.LadderUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.dto.LadderTickDto;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundType;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.utils.FormattingUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class LadderEventService {

  private static final double NANOS_IN_SECONDS = TimeUnit.SECONDS.toNanos(1);
  private final AccountService accountService;
  private final ChatService chatService;

  private final MessageService messageService;
  private final LadderService ladderService;
  private final RoundService roundService;
  private final WsUtils wsUtils;
  private final LadderUtils ladderUtils;
  private final UpgradeUtils upgradeUtils;
  private final FairConfig fairConfig;
  private final Gson gson;
  private final CriticalRegion semaphore = new CriticalRegion(1);
  private final Map<Integer, List<Event<LadderEventTypes>>> eventMap = new HashMap<>();
  private long lastTickInNanos = System.nanoTime();


  public LadderEventService(AccountService accountService, ChatService chatService,
      MessageService messageService, LadderService ladderService,
      RoundService roundService, WsUtils wsUtils, LadderUtils ladderUtils,
      UpgradeUtils upgradeUtils, FairConfig fairConfig, Gson gson) {
    this.accountService = accountService;
    this.chatService = chatService;
    this.messageService = messageService;
    this.ladderService = ladderService;
    this.roundService = roundService;
    this.wsUtils = wsUtils;
    this.ladderUtils = ladderUtils;
    this.upgradeUtils = upgradeUtils;
    this.fairConfig = fairConfig;
    this.gson = gson;
  }

  @Scheduled(initialDelay = 1000, fixedRate = 1000)
  public void update() {
    try (var ignored = ladderService.getSemaphore().enter()) {
      handlePlayerEvents();

      // Calculate Time passed
      long currentTimeInNanos = System.nanoTime();
      double deltaInSeconds = Math.max(
          (currentTimeInNanos - lastTickInNanos) / NANOS_IN_SECONDS,
          1.0d
      );
      lastTickInNanos = currentTimeInNanos;

      // Send the tick for everyone
      LadderTickDto tickDto = new LadderTickDto();
      tickDto.setDelta(deltaInSeconds);
      wsUtils.convertAndSendToTopic(FairController.TOPIC_TICK_DESTINATION, tickDto);

      // Calculate the tick yourself
      Collection<LadderEntity> ladders = ladderService.getCurrentLadderMap().values();
      List<CompletableFuture<Void>> futures = ladders.stream()
          .map(ladder -> CompletableFuture.runAsync(
              () -> calculateLadder(ladder, deltaInSeconds))).toList();
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

    } catch (ExecutionException | InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }

  private void handlePlayerEvents() throws InterruptedException {
    try (var ignored = semaphore.enter()) {
      for (int i = 1; i <= ladderService.getCurrentLadderMap().size(); i++) {
        LadderEntity ladder = ladderService.getCurrentLadderMap().get(i);
        List<Event<LadderEventTypes>> events = ladderService.getEventMap()
            .get(ladder.getNumber());
        List<Event<LadderEventTypes>> eventsToBeRemoved = new ArrayList<>();
        for (Event<LadderEventTypes> e : events) {
          if (BUY_BIAS.equals(e.getEventType())) {
            if (!buyBias(e, ladder)) {
              eventsToBeRemoved.add(e);
            }
          } else if (BUY_MULTI.equals(e.getEventType())) {
            if (!buyMulti(e, ladder)) {
              eventsToBeRemoved.add(e);
            }
          } else if (PROMOTE.equals(e.getEventType())) {
            if (!promote(e, ladder)) {
              eventsToBeRemoved.add(e);
            }
          } else if (THROW_VINEGAR.equals(e.getEventType())) {
            if (!throwVinegar(e, ladder)) {
              eventsToBeRemoved.add(e);
            }
          } else if (BUY_AUTO_PROMOTE.equals(e.getEventType())) {
            if (!buyAutoPromote(e, ladder)) {
              eventsToBeRemoved.add(e);
            }
          }
        }
        for (Event<LadderEventTypes> e : eventsToBeRemoved) {
          events.remove(e);
        }
      }
      ladderService.getEventMap().values().forEach(List::clear);
    }
  }

  private void calculateLadder(LadderEntity ladder, double delta) {
    List<RankerEntity> rankers = ladder.getRankers();
    rankers.sort(Comparator.comparing(RankerEntity::getPoints).reversed());

    for (int i = 0; i < rankers.size(); i++) {
      RankerEntity currentRanker = rankers.get(i);
      currentRanker.setRank(i + 1);
      // if the ranker is currently still on the ladder
      if (currentRanker.isGrowing()) {
        // Calculating points & Power
        if (currentRanker.getRank() != 1) {
          currentRanker.addPower(
              (i + currentRanker.getBias()) * currentRanker.getMultiplier(), delta);
        }
        currentRanker.addPoints(currentRanker.getPower(), delta);

        // Calculating Vinegar based on Grapes count
        if (currentRanker.getRank() != 1) {
          currentRanker.addVinegar(currentRanker.getGrapes(), delta);
        }
        if (currentRanker.getRank() == 1 && ladderUtils.isLadderPromotable(ladder)) {
          currentRanker.mulVinegar(0.9975, delta);
        }

        for (int j = i - 1; j >= 0; j--) {
          // If one of the already calculated Rankers have less points than this ranker
          // swap these in the list... This way we keep the list sorted, theoretically
          if (currentRanker.getPoints().compareTo(rankers.get(j).getPoints()) > 0) {
            // Move 1 Position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            RankerEntity temp = rankers.get(j);
            temp.setRank(j + 2);
            if (temp.isGrowing() && temp.getMultiplier() > 1) {
              temp.setGrapes(temp.getGrapes().add(BigInteger.valueOf(ladder.getPassingGrapes())));
            }
            rankers.set(j + 1, temp);

            // Move this Ranker 1 Place up
            currentRanker.setRank(j + 1);
            rankers.set(j, currentRanker);
          } else {
            break;
          }
        }
      }
    }
    // Ranker on Last Place gains 1 Grape, even if hes also in first at the same time (ladder of 1)
    if (rankers.size() >= 1) {
      RankerEntity lastRanker = rankers.get(rankers.size() - 1);
      if (lastRanker.isGrowing()) {
        lastRanker.addGrapes(BigInteger.valueOf(ladder.getBottomGrapes()), delta);
      }
    }

    if (rankers.size() >= 1 && (rankers.get(0).isAutoPromote() || ladder.getTypes()
        .contains(LadderType.FREE_AUTO)) && rankers.get(0).isGrowing()
        && ladderUtils.isLadderPromotable(ladder)) {
      addEvent(ladder.getNumber(), new Event<>(PROMOTE, rankers.get(0).getAccountId()));
    }
  }

  /**
   * Adds an event to the list of events inside the eventMap. This calls a semaphore and should
   * thereby only be done by the Controllers/API.
   *
   * @param event the event that gets added to the eventMap
   */
  public void addEvent(int ladderNumber, Event<LadderEventTypes> event) {
    try (var ignored = semaphore.enter()) {

      eventMap.get(ladderNumber).add(event);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }


  /**
   * Buy Bias for the active ranker of an account on a specific ladder.
   *
   * @param event  the event that contains the information for the buy
   * @param ladder the ladder the ranker is on
   * @return if the ranker can buy bias
   */
  private boolean buyBias(Event<LadderEventTypes> event, LadderEntity ladder) {
    try {
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      BigInteger cost = upgradeUtils.buyUpgradeCost(ladder.getScaling(), ranker.getBias(),
          ladder.getTypes());
      if (ranker.getPoints().compareTo(cost) >= 0) {
        // TODO: statisticsService.recordBias(ranker, ladder, currentRound);
        ranker.setPoints(BigInteger.ZERO);
        ranker.setBias(ranker.getBias() + 1);
        wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
            ladder.getNumber(), event);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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
  private boolean buyMulti(Event<LadderEventTypes> event, LadderEntity ladder) {
    try {
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      BigInteger cost = upgradeUtils.buyUpgradeCost(ladder.getScaling(), ranker.getMultiplier(),
          ladder.getTypes());
      if (ranker.getPower().compareTo(cost) >= 0) {
        // TODO: statisticsService.recordMulti(ranker, ladder, currentRound);
        ranker.setPoints(BigInteger.ZERO);
        ranker.setPower(BigInteger.ZERO);
        ranker.setBias(0);
        ranker.setMultiplier(ranker.getMultiplier() + 1);
        wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
            ladder.getNumber(), event);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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
  boolean buyAutoPromote(Event<LadderEventTypes> event, LadderEntity ladder) {
    try {
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      if (ranker == null) {
        return false;
      }

      BigInteger cost = upgradeUtils.buyAutoPromoteCost(roundService.getCurrentRound(), ladder,
          ranker.getRank());

      if (ladder.getTypes().contains(LadderType.FREE_AUTO)) {
        ranker.setAutoPromote(true);

        // TODO: Get Account of Ranker -> UUID
        // wsUtils.convertAndSendToUser(ranker.getAccountId().getUuid(), LadderController.PRIVATE_EVENTS_DESTINATION, event);

        return true;
      }

      if (ladderUtils.canBuyAutoPromote(ladder, ranker, roundService.getCurrentRound())) {
        // TODO: statisticsService.recordAutoPromote(ranker, ladder, currentRound);
        ranker.setGrapes(ranker.getGrapes().subtract(cost));
        ranker.setAutoPromote(true);
        // TODO: Get Account of Ranker -> UUID
        // wsUtils.convertAndSendToUser(ranker.getAccountId().getUuid(), LadderController.PRIVATE_EVENTS_DESTINATION, event);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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
  boolean promote(Event<LadderEventTypes> event, LadderEntity ladder) {
    try {
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      if (ladderUtils.canPromote(ladder, ranker)) {
        // TODO: statisticsService.recordPromote(ranker, ladder, currentRound);
        AccountEntity account = accountService.find(ranker.getAccountId());
        log.info("[L{}] Promotion for {} (#{})", ladder.getNumber(), account.getDisplayName(),
            account.getId());
        ranker.setGrowing(false);

        RoundEntity round = ladder.getRound();

        RankerEntity newRanker = ladderService.createRankerOnLadder(account,
            ladder.getNumber() + 1);
        newRanker.setVinegar(ranker.getVinegar());
        newRanker.setGrapes(ranker.getGrapes());
        newRanker.getUnlocks().copy(ranker.getUnlocks());
        LadderEntity newLadder = ladderService.find(newRanker.getLadderId());

        // Auto-Ladder
        Integer number = Math.floorDiv(newLadder.getNumber(), 2) - 2;
        LadderEntity autoLadder = ladderService.findInCache(number);
        if (autoLadder != null && !autoLadder.getTypes().contains(LadderType.FREE_AUTO)
            && !autoLadder.getTypes().contains(LadderType.NO_AUTO)) {
          autoLadder.getTypes().add(LadderType.FREE_AUTO);
          Event<LadderEventTypes> e = new Event<>(LadderEventTypes.UPDATE_TYPES, account.getId(),
              autoLadder.getTypes());
          wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
              autoLadder.getNumber(), e);
        }

        // Special_100 Logic
        if (round.getTypes().contains(RoundType.SPECIAL_100) && newLadder.getTypes()
            .contains(LadderType.END)) {
          LadderEntity assholeLadder = ladderService.findInCache(
              round.getModifiedBaseAssholeLadder());
          assholeLadder.getTypes().remove(LadderType.DEFAULT);
          assholeLadder.getTypes().add(LadderType.ASSHOLE);

          assholeLadder.getRankers().forEach(r -> {
            AccountEntity a = accountService.find(r.getAccountId());
            RankerEntity highestRanker = ladderService.findFirstActiveRankerOfAccountThisRound(a);
            if (!r.isGrowing()) {
              a.getAchievements().setPressedAssholeButton(true);
              highestRanker.getUnlocks().setPressedAssholeButton(true);
            }
            highestRanker.getUnlocks().setReachedAssholeLadder(true);
          });

          Event<LadderEventTypes> e = new Event<>(LadderEventTypes.UPDATE_TYPES, account.getId(),
              assholeLadder.getTypes());
          wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
              assholeLadder.getNumber(), e);
        }

        // Unlocks
        if (!newRanker.getUnlocks().getAutoPromote()
            && newLadder.getNumber() >= fairConfig.getAutoPromoteLadder()) {
          newRanker.getUnlocks().setAutoPromote(true);
        }
        if (!newRanker.getUnlocks().getReachedBaseAssholeLadder()
            && newLadder.getNumber() >= roundService.getCurrentRound()
            .getModifiedBaseAssholeLadder()) {
          newRanker.getUnlocks().setReachedBaseAssholeLadder(true);
        }
        if (!newRanker.getUnlocks().getReachedAssholeLadder()
            && newLadder.getTypes().contains(LadderType.ASSHOLE)) {
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
              newRanker.getVinegar().multiply(BigInteger.valueOf(newLadder.getWinningMultiplier()))
                  .divide(BigInteger.TEN));
        }

        newRanker.setGrapes(newRanker.getGrapes().add(BigInteger.valueOf(
            newLadder.getWinningGrapes(newLadder.getRankers().size(),
                fairConfig.getBaseGrapesToBuyAutoPromote()))));

        wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
            ladder.getNumber(), event);
        wsUtils.convertAndSendToUser(account.getUuid(),
            AccountController.PRIVATE_EVENTS_DESTINATION, new Event<>(
                AccountEventTypes.INCREASE_HIGHEST_LADDER, account.getId(), newLadder.getNumber()
            ));
        account = accountService.save(account);

        // Logic for the Asshole-Ladder
        if (ladder.getTypes().contains(LadderType.ASSHOLE) && newRanker.getUnlocks()
            .getPressedAssholeButton()) {
          JsonObject object1 = new JsonObject();
          object1.addProperty("u", account.getDisplayName());
          object1.addProperty("id", account.getId());
          object1.addProperty("i", 0);

          AccountEntity broadCaster = accountService.findBroadcaster();
          JsonObject object2 = new JsonObject();
          object2.addProperty("u", broadCaster.getDisplayName());
          object2.addProperty("id", broadCaster.getId());
          object2.addProperty("i", 20);

          String metadataString = gson.toJson(new JsonObject[]{object1, object2});

          ChatEntity chat = chatService.find(ChatType.SYSTEM);

          messageService.create(accountService.findBroadcaster(), chat, FormattingUtils.format(
              "{@} was welcomed by {@}. They are the {} lucky initiate for the {} big ritual.",
              FormattingUtils.ordinal(newLadder.getRankers().size()),
              FormattingUtils.ordinal(roundService.getCurrentRound().getNumber())
          ), metadataString);

          int neededAssholesForReset = roundService.getCurrentRound().getAssholesForReset();
          int assholeCount = newLadder.getRankers().size();

          // Is it time to reset the game
          if (assholeCount >= neededAssholesForReset || round.getTypes()
              .contains(RoundType.SPECIAL_100)) {
            wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION,
                new Event<>(RoundEventTypes.RESET, account.getId()));

            LadderEntity firstLadder = ladderService.findInCache(1);
            List<AccountEntity> accounts = firstLadder.getRankers()
                .stream()
                .map((r) -> accountService.find(r.getAccountId()))
                .distinct().toList();
            for (AccountEntity entity : accounts) {
              RankerEntity highestRanker = ladderService.findFirstActiveRankerOfAccountThisRound(
                  entity);
              UnlocksEntity unlocks = highestRanker.getUnlocks();
              entity.setAssholePoints(entity.getAssholePoints() + unlocks.calculateAssholePoints());
            }

            accountService.save(accounts);
          }
        }
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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
  boolean throwVinegar(Event<LadderEventTypes> event, LadderEntity ladder) {
    try {
      ladder = ladderService.find(ladder);
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      RankerEntity target = ladder.getRankers().get(0);
      AccountEntity rankerAccount = accountService.find(ranker.getAccountId());
      AccountEntity targetAccount = accountService.find(target.getAccountId());

      if (target.isAutoPromote() || ladder.getTypes().contains(LadderType.FREE_AUTO)) {
        log.info("[L{}] {} (#{}) tried to throw Vinegar at {} (#{}), but they had Auto-Promote!",
            ladder.getNumber(), rankerAccount.getDisplayName(), rankerAccount.getId(),
            targetAccount.getDisplayName(), targetAccount.getId());
        return false;
      }

      if (ladderUtils.canThrowVinegarAt(ladder, ranker, target)) {
        // TODO: statisticsService.recordVinegarThrow(ranker, target, ladder, currentRound);
        BigInteger rankerVinegar = ranker.getVinegar();
        BigInteger targetVinegar = target.getVinegar();

        log.info("[L{}] {} (#{}) is using their {} Vinegar on {} (#{}) with {} Vinegar",
            ladder.getNumber(), rankerAccount.getDisplayName(), rankerAccount.getId(),
            rankerVinegar,
            targetAccount.getDisplayName(), targetAccount.getId(), targetVinegar);

        VinegarData data = new VinegarData(rankerVinegar.toString(), targetAccount.getId());
        if (targetVinegar.compareTo(rankerVinegar) > 0) {
          targetVinegar = targetVinegar.subtract(rankerVinegar);
        } else {
          targetVinegar = BigInteger.ZERO;
          data.setSuccess(true);
        }

        event.setData(data);
        wsUtils.convertAndSendToUser(accountService.find(ranker.getAccountId()).getUuid(),
            LadderController.PRIVATE_EVENTS_DESTINATION, event);
        wsUtils.convertAndSendToUser(accountService.find(target.getAccountId()).getUuid(),
            LadderController.PRIVATE_EVENTS_DESTINATION, event);

        if (data.isSuccess()) {
          removeMulti(new Event<>(LadderEventTypes.REMOVE_MULTI, targetAccount.getId()), ladder);
        }

        ranker.setVinegar(BigInteger.ZERO);
        target.setVinegar(targetVinegar);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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
  boolean softResetPoints(Event<LadderEventTypes> event, LadderEntity ladder) {
    try {
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      ranker.setPoints(BigInteger.ZERO);
      ranker.setPower(ranker.getPower().divide(BigInteger.TWO));
      wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
          ladder.getNumber(), event);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return false;
  }

  /**
   * Removes 1 Multi from the active ranker of an account on a specific ladder. This mainly happens
   * after successfully thrown vinegar.
   *
   * @param event  the event that contains the information for the removal
   * @param ladder the ladder the ranker is on
   * @return if the ranker can be soft-reset
   */
  boolean removeMulti(Event event, LadderEntity ladder) {
    try {
      RankerEntity ranker = ladderService.findActiveRankerOfAccountOnLadder(event.getAccountId(),
          ladder);
      ranker.setMultiplier(Math.max(1, ranker.getMultiplier() - 1));
      ranker.setBias(0);
      ranker.setPower(BigInteger.ZERO);
      ranker.setPoints(BigInteger.ZERO);
      wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION,
          ladder.getNumber(), event);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return false;
  }
}
