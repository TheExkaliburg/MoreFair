package de.kaliburg.morefair.schedules;

import de.kaliburg.morefair.controller.FairController;
import de.kaliburg.morefair.controller.RankerController;
import de.kaliburg.morefair.dto.EventDTO;
import de.kaliburg.morefair.dto.HeartbeatDTO;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // TODO: Change the algorithm, that it only needs to read from the DB when theres an update to one of the Rankers
    //  That way i would get rid of the O(r log r);
    // O(l * (r log r + (r * r/2) + r)) = O ( l * r * r)
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
                        for (Ladder ladder : rankerService.getLadders()) {
                            // Handle the events from the last Second
                            List<EventDTO> events = rankerService.getEventMap().get(ladder.getNumber());
                            for (EventDTO e : events) {
                                switch (e.getEventType()) {
                                    case BIAS -> {
                                        if (!rankerService.buyBias(e.getRankerId(), ladder)) events.remove(e);
                                    }
                                    case MULTI -> {
                                        if (!rankerService.buyMulti(e.getRankerId(), ladder)) events.remove(e);
                                    }
                                    case PROMOTE -> {
                                        if (!rankerService.promote(e.getRankerId(), ladder)) events.remove(e);
                                    }
                                    case ASSHOLE -> {
                                        if (!rankerService.beAsshole(e.getRankerId(), ladder)) events.remove(e);
                                        else didPressAssholeButton = true;
                                    }
                                    case VINEGAR -> {
                                        if (!rankerService.throwVinegar(e.getRankerId(), ladder)) events.remove(e);
                                    }
                                    default -> {
                                        events.remove(e);
                                    }
                                }
                            }
                            heartbeatMap.put(ladder.getNumber(), new HeartbeatDTO(events));
                        }
                    } finally {
                        rankerService.getEventSem().release();
                    }
                } catch (InterruptedException ignored) {

                }

                // Calculate Time passed
                long currentNanos = System.nanoTime();
                double deltaSec = (currentNanos - lastTimeMeasured) / NANOS_IN_SECONDS;
                lastTimeMeasured = currentNanos;

                // Send Broadcasts

                // If someone was an Asshole and the reset worked, should notify all and end calculation
                if (didPressAssholeButton && rankerService.resetAllLadders()) {
                    for (Ladder ladder : rankerService.getLadders()) {
                        heartbeatMap.get(ladder.getNumber()).setSecondsPassed(deltaSec);
                        wsUtils.convertAndSendToAll(RankerController.LADDER_UPDATE_DESTINATION + ladder.getNumber(), "RESET");
                    }
                    return;
                }

                // Otherwise, just send the default Broadcasts
                for (Ladder ladder : rankerService.getLadders()) {
                    heartbeatMap.get(ladder.getNumber()).setSecondsPassed(deltaSec);
                    wsUtils.convertAndSendToAll(RankerController.LADDER_UPDATE_DESTINATION + ladder.getNumber(), heartbeatMap.get(ladder.getNumber()));
                }

                // Calculate Ladder yourself
                List<Ladder> ladders = rankerService.getLadders();
                List<CompletableFuture<Void>> futures = ladders.stream()
                        .map(ladder -> CompletableFuture.runAsync(() -> calculateLadder(ladder)))
                        .toList();
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                rankerService.getLadderSem().release();
            }
        } catch (InterruptedException ignored) {

        }
    }

    private void calculateLadder(Ladder ladder) {
        List<Ranker> rankers = ladder.getRankers();
        rankers.sort(Comparator.comparing(Ranker::getPoints).reversed());
        for (int i = 0; i < rankers.size(); i++) {
            Ranker currentRanker = rankers.get(i);
            currentRanker.setRank(i + 1);
            // if the ranker is currently still on the ladder
            if (currentRanker.isGrowing()) {
                // Calculating points & Power
                if (currentRanker.getRank() != 1)
                    currentRanker.addPower((i + currentRanker.getBias()) * currentRanker.getMultiplier());
                currentRanker.addPoints(currentRanker.getPower());

                // Calculating Vinegar based on Grapes count
                currentRanker.setVinegar(currentRanker.getVinegar().add(currentRanker.getGrapes()));

                for (int j = i - 1; j >= 0; j--) {
                    // If one of the already calculated Rankers have less points than this ranker
                    // swap these in the list... This way we keep the list sorted, theoretically
                    if (currentRanker.getPoints().compareTo(rankers.get(j).getPoints()) > 0) {
                        // Move 1 Position up and move the ranker there 1 Position down

                        // Move other Ranker 1 Place down
                        Ranker temp = rankers.get(j);
                        temp.setRank(j + 2);
                        if (temp.isGrowing()) temp.setGrapes(temp.getGrapes().add(BigInteger.ONE));
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
        // Ranker on Last Place gains 1 Grape, only if he isn't the only one
        if (rankers.size() >= FairController.PEOPLE_FOR_PROMOTE) {
            Ranker lastRanker = rankers.get(rankers.size() - 1);
            lastRanker.setGrapes(lastRanker.getGrapes().add(BigInteger.ONE));
        }
    }
}



