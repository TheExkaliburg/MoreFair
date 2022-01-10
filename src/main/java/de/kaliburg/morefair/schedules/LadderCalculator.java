package de.kaliburg.morefair.schedules;

import de.kaliburg.morefair.controller.FairController;
import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.persistence.entity.Ladder;
import de.kaliburg.morefair.persistence.entity.Ranker;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Log4j2
@Component
@EnableScheduling
public class LadderCalculator {
    private final LadderService ladderService;
    private final RankerService rankerService;
    private final AccountService accountService;

    public LadderCalculator(LadderService ladderService, RankerService rankerService, AccountService accountService) {
        this.ladderService = ladderService;
        this.rankerService = rankerService;
        this.accountService = accountService;
    }

    // TODO: Change the algorithm, that it only needs to read from the DB when theres an update to one of the Rankers
    //  That way i would get rid of the O(r log r);
    // O(l * (r log r + (r * r/2) + r)) = O ( l * r * r)
    @Scheduled(initialDelay = 15000, fixedRate = 1000)
    public void calc() {
        List<Ladder> ladders = ladderService.findAllLadders();
        for (Ladder ladder : ladders) {
            DatabaseWriteSemaphore.getInstance().aquireAndAutoReleaseSilent(() -> {
                // Read from DB here
                List<Ranker> rankers = rankerService.findAllRankerByLadderOrderedByPoints(ladder);
                int growingRankerCount = 0;
                for (int i = 0; i < rankers.size(); i++) {
                    Ranker currentRanker = rankers.get(i);
                    currentRanker.setRank(i + 1);
                    // if the ranker is currently still on the ladder
                    if (currentRanker.isGrowing()) {
                        growingRankerCount++;

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

                // Write to database here
                rankerService.saveAllRankerStats(rankers);
                ladderService.save(ladder.setSize(rankers.size()).setGrowingRankerCount(growingRankerCount));
            });
        }
    }
}

