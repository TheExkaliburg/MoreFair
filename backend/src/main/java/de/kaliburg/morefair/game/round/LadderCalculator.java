package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.RankerController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.dto.HeartbeatDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.chat.MessageService;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@EnableScheduling
public class LadderCalculator {

  private static final double NANOS_IN_SECONDS = TimeUnit.SECONDS.toNanos(1);
  private final LadderService ladderService;
  private final AccountService accountService;
  private final MessageService messageService;
  private final WsUtils wsUtils;
  private final LadderUtils ladderUtils;
  private final RoundService roundService;
  private final RoundUtils roundUtils;
  private Map<Integer, HeartbeatDTO> heartbeatMap = new HashMap<>();
  private boolean didPressAssholeButton = false;
  private long lastTimeMeasured = System.nanoTime();

  public LadderCalculator(LadderService ladderService, AccountService accountService,
      WsUtils wsUtils,
      MessageService messageService, LadderUtils ladderUtils, RoundService roundService,
      RoundUtils roundUtils) {
    this.ladderService = ladderService;
    this.accountService = accountService;
    this.wsUtils = wsUtils;
    this.messageService = messageService;
    this.ladderUtils = ladderUtils;
    this.roundService = roundService;
    this.roundUtils = roundUtils;
  }

  @Scheduled(initialDelay = 1000, fixedRate = 1000)
  public void update() {
    // Reset the Heartbeat
    heartbeatMap = new HashMap<>();
    didPressAssholeButton = false;
    try {
      ladderService.getLadderSemaphore().acquire();
      try {
        // Process and filter all events since the last Calculation Step
        handlePlayerEvents();

        // Calculate Time passed
        long currentNanos = System.nanoTime();
        double deltaSec = Math.max((currentNanos - lastTimeMeasured) / NANOS_IN_SECONDS,
            1.0d);
        lastTimeMeasured = currentNanos;

        // Send Broadcasts

        // If someone was an Asshole and the reset worked, should notify all and end calculation
        if (didPressAssholeButton) {
          // TODO: Reset Game Logic
        }

        // Otherwise, just send the default Broadcasts
        for (LadderEntity ladder : ladderService.getCurrentLadderMap().values()) {
          heartbeatMap.get(ladder.getNumber()).setSecondsPassed(deltaSec);
          wsUtils.convertAndSendToTopic(
              RankerController.LADDER_UPDATE_DESTINATION + ladder.getNumber(),
              heartbeatMap.get(ladder.getNumber()));
        }

        // Calculate Ladder yourself
        Collection<LadderEntity> ladders = ladderService.getCurrentLadderMap().values();
        List<CompletableFuture<Void>> futures = ladders.stream()
            .map(ladder -> CompletableFuture.runAsync(
                () -> calculateLadder(ladder, deltaSec))).toList();
        try {
          CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (ExecutionException | InterruptedException e) {
          log.error(e.getMessage());
          e.printStackTrace();
        }
      } finally {
        ladderService.getLadderSemaphore().release();
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  private void handlePlayerEvents() {
    try {
      ladderService.getEventSemaphore().acquire();
      try {
        for (int i = 1; i <= ladderService.getCurrentLadderMap().size(); i++) {
          // Handle the events since the last update
          LadderEntity ladder = ladderService.getCurrentLadderMap().get(i);
          List<Event> events = ladderService.getEventMap().get(ladder.getNumber());
          List<Event> eventsToBeRemoved = new ArrayList<>();
          for (int j = 0; j < events.size(); j++) {
            Event e = events.get(j);
            switch (e.getEventType()) {
              case BUY_BIAS -> {
                if (!ladderService.buyBias(e.getAccountId(), ladder)) {
                  eventsToBeRemoved.add(e);
                }
              }
              case BUY_MULTI -> {
                if (!ladderService.buyMulti(e.getAccountId(), ladder)) {
                  eventsToBeRemoved.add(e);
                }
              }
              case PROMOTE -> {
                if (!ladderService.promote(e.getAccountId(), ladder)) {
                  eventsToBeRemoved.add(e);
                } else if (roundUtils.getAssholeLadderNumber(roundService.getCurrentRound())
                    .equals(ladder.getNumber())) {
                  didPressAssholeButton = true;
                }
              }
              case THROW_VINEGAR -> {
                if (!ladderService.throwVinegar(e.getAccountId(), ladder, e)) {
                  eventsToBeRemoved.add(e);
                }
              }
              case BUY_AUTO_PROMOTE -> {
                if (!ladderService.buyAutoPromote(e.getAccountId(), ladder)) {
                  eventsToBeRemoved.add(e);
                }
              }
              default -> {

              }
            }
          }
          for (Event e : eventsToBeRemoved) {
            events.remove(e);
          }
          heartbeatMap.put(ladder.getNumber(), new HeartbeatDTO(new ArrayList<>(events)));
        }
        ladderService.getEventMap().values().forEach(List::clear);
      } finally {
        ladderService.getEventSemaphore().release();
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  private void calculateLadder(LadderEntity ladder, double deltaSec) {
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
              (i + currentRanker.getBias()) * currentRanker.getMultiplier(), deltaSec);
        }
        currentRanker.addPoints(currentRanker.getPower(), deltaSec);

        // Calculating Vinegar based on Grapes count
        if (currentRanker.getRank() != 1) {
          currentRanker.addVinegar(currentRanker.getGrapes(), deltaSec);
        }
        if (currentRanker.getRank() == 1 && ladderUtils.isLadderPromotable(ladder)) {
          currentRanker.mulVinegar(0.9975, deltaSec);
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
              temp.setGrapes(temp.getGrapes().add(BigInteger.ONE));
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
    // Ranker on Last Place gains 1 Grape, only if he isn't in the top group
    if (rankers.size() >= ladder.getRequiredRankerCountToUnlock()) {
      RankerEntity lastRanker = rankers.get(rankers.size() - 1);
      lastRanker.addGrapes(BigInteger.valueOf(3), deltaSec);
    }

    if (rankers.size() >= 1 && rankers.get(0).isAutoPromote() && rankers.get(0).isGrowing()
        && rankers.get(0).getPoints().compareTo(ladder.getRequiredPointsToUnlock()) >= 0
        && rankers.size() >= ladder.getRequiredRankerCountToUnlock()) {
      log.info("[L{}] Trying to auto-promote {} (#{})", ladder.getNumber(),
          rankers.get(0).getAccount().getUsername(), rankers.get(0).getAccount().getId());
      ladderService.addEvent(ladder.getNumber(),
          new Event(EventType.PROMOTE, rankers.get(0).getAccount().getId()));
    }
  }
}



