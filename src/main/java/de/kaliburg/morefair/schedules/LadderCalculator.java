package de.kaliburg.morefair.schedules;

import de.kaliburg.morefair.controller.FairController;
import de.kaliburg.morefair.controller.RankerController;
import de.kaliburg.morefair.dto.HeartbeatDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.EventType;
import de.kaliburg.morefair.persistence.entity.Ladder;
import de.kaliburg.morefair.persistence.entity.Ranker;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.RankerService;
import de.kaliburg.morefair.utils.WSUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
@EnableScheduling
public class LadderCalculator {
    private static final double NANOS_IN_SECONDS = TimeUnit.SECONDS.toNanos(1);
    private final RankerService rankerService;
    private final AccountService accountService;
    private final WSUtils wsUtils;
    private Map<Integer, HeartbeatDTO> heartbeatMap = new HashMap<>();
    private long lastTimeMeasured = System.nanoTime();

    public LadderCalculator(RankerService rankerService, AccountService accountService, WSUtils wsUtils) {
        this.rankerService = rankerService;
        this.accountService = accountService;
        this.wsUtils = wsUtils;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 1000)
    public void update() {
        // Reset the Heartbeat
        heartbeatMap = new HashMap<>();
        boolean didPressAssholeButton = false;
        try {
            rankerService.getLadderSem().acquire();
            try {
                // Process and filter all events since the last Calculation Step
                try {
                    rankerService.getEventSem().acquire();
                    try {
                        for (int i = 1; i <= rankerService.getLadders().size(); i++) {
                            // Handle the events since the last update
                            Ladder ladder = rankerService.getLadders().get(i);
                            List<Event> events = rankerService.getEventMap().get(ladder.getNumber());
                            List<Event> eventsToBeRemoved = new ArrayList<>();
                            for (int j = 0; j < events.size(); j++) {
                                Event e = events.get(j);
                                switch (e.getEventType()) {
                                    case BIAS -> {
                                        if (!rankerService.buyBias(e.getAccountId(), ladder))
                                            eventsToBeRemoved.add(e);
                                    }
                                    case MULTI -> {
                                        if (!rankerService.buyMulti(e.getAccountId(), ladder))
                                            eventsToBeRemoved.add(e);
                                    }
                                    case PROMOTE -> {
                                        if (!rankerService.promote(e.getAccountId(), ladder))
                                            eventsToBeRemoved.add(e);
                                    }
                                    case ASSHOLE -> {
                                        eventsToBeRemoved.add(e);
                                        if (rankerService.beAsshole(e.getAccountId(), ladder))
                                            didPressAssholeButton = true;
                                    }
                                    case VINEGAR -> {
                                        if (!rankerService.throwVinegar(e.getAccountId(), ladder, e))
                                            eventsToBeRemoved.add(e);
                                    }
                                    case NAME_CHANGE -> {
                                        accountService.updateUsername(e.getAccountId(), ladder, e);
                                    }
                                    case AUTO_PROMOTE -> {
                                        if (!rankerService.buyAutoPromote(e.getAccountId(), ladder))
                                            eventsToBeRemoved.add(e);
                                    }
                                    case SOFT_RESET_POINTS -> {
                                        rankerService.softResetPoints(e.getAccountId(), ladder);
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
                        rankerService.resetEvents();
                    } finally {
                        rankerService.getEventSem().release();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }

                // Calculate Time passed
                long currentNanos = System.nanoTime();
                double deltaSec = Math.max((currentNanos - lastTimeMeasured) / NANOS_IN_SECONDS, 1.0d);
                lastTimeMeasured = currentNanos;

                // Send Broadcasts

                // If someone was an Asshole and the reset worked, should notify all and end calculation
                if (didPressAssholeButton && rankerService.resetAllLadders()) {
                    for (Ladder ladder : rankerService.getLadders().values()) {
                        heartbeatMap.get(ladder.getNumber()).setSecondsPassed(deltaSec);
                        heartbeatMap.get(ladder.getNumber()).setEvents(new ArrayList<>());
                        heartbeatMap.get(ladder.getNumber()).getEvents().add(new Event(EventType.RESET, 0L));
                        wsUtils.convertAndSendToAll(RankerController.LADDER_UPDATE_DESTINATION + ladder.getNumber(), heartbeatMap.get(ladder.getNumber()));
                    }
                    return;
                }

                // Otherwise, just send the default Broadcasts
                for (Ladder ladder : rankerService.getLadders().values()) {
                    heartbeatMap.get(ladder.getNumber()).setSecondsPassed(deltaSec);
                    wsUtils.convertAndSendToAll(RankerController.LADDER_UPDATE_DESTINATION + ladder.getNumber(), heartbeatMap.get(ladder.getNumber()));
                }

                // Calculate Ladder yourself
                Collection<Ladder> ladders = rankerService.getLadders().values();
                List<CompletableFuture<Void>> futures = ladders.stream()
                        .map(ladder -> CompletableFuture.runAsync(() -> calculateLadder(ladder, deltaSec)))
                        .toList();
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                } catch (ExecutionException | InterruptedException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            } finally {
                rankerService.getLadderSem().release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void calculateLadder(Ladder ladder, double deltaSec) {
        List<Ranker> rankers = ladder.getRankers();
        rankers.sort(Comparator.comparing(Ranker::getPoints).reversed());
        for (int i = 0; i < rankers.size(); i++) {
            Ranker currentRanker = rankers.get(i);
            currentRanker.setRank(i + 1);
            // if the ranker is currently still on the ladder
            if (currentRanker.isGrowing()) {
                // Calculating points & Power
                if (currentRanker.getRank() != 1)
                    currentRanker.addPower((i + currentRanker.getBias()) * currentRanker.getMultiplier(), deltaSec);
                currentRanker.addPoints(currentRanker.getPower(), deltaSec);

                // Calculating Vinegar based on Grapes count
                if (currentRanker.getRank() != 1)
                    currentRanker.addVinegar(currentRanker.getGrapes(), deltaSec);

                for (int j = i - 1; j >= 0; j--) {
                    // If one of the already calculated Rankers have less points than this ranker
                    // swap these in the list... This way we keep the list sorted, theoretically
                    if (currentRanker.getPoints().compareTo(rankers.get(j).getPoints()) > 0) {
                        // Move 1 Position up and move the ranker there 1 Position down

                        // Move other Ranker 1 Place down
                        Ranker temp = rankers.get(j);
                        temp.setRank(j + 2);
                        if (temp.isGrowing() && (temp.getBias() > 0 || temp.getMultiplier() > 1))
                            temp.setGrapes(temp.getGrapes().add(BigInteger.ONE));
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
        if (rankers.size() >= Math.max(FairController.MINIMUM_PEOPLE_FOR_PROMOTE, ladder.getNumber())) {
            Ranker lastRanker = rankers.get(rankers.size() - 1);
            lastRanker.addGrapes(BigInteger.ONE, deltaSec);
        }

        if (rankers.size() >= 1 && rankers.get(0).isAutoPromote() && rankers.get(0).isGrowing()
                && rankers.get(0).getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0
                && rankers.size() >= Math.max(ladder.getNumber(), FairController.MINIMUM_PEOPLE_FOR_PROMOTE)) {
            log.info("[L{}] Trying to auto-promote {}", ladder.getNumber(), rankers.get(0).getAccount().getUsername());
            rankerService.addEvent(ladder.getNumber(), new Event(EventType.PROMOTE, rankers.get(0).getAccount().getId()));
        }
    }
}



