package de.kaliburg.morefair.schedules;

import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        for (Ladder ladder : ladders) {        //O(l)
            DatabaseWriteSemaphore.getInstance().aquireAndAutoReleaseSilent(() -> {
                // Read from DB here
                List<Ranker> rankers = rankerService.findAllRankerByLadderOrderedByPoints(ladder);          //  O(r log r)
                int growingRankerCount = 0;
                for (int i = 0; i < rankers.size(); i++) {                                                      //  O(r)
                    Ranker currentRanker = rankers.get(i);
                    // if the ranker is currently still on the ladder
                    if (currentRanker.isGrowing()) {
                        growingRankerCount++;

                        // Calculating points & Power
                        if (currentRanker.getRank() != 1)
                            currentRanker.addPower((i + currentRanker.getBias()) * currentRanker.getMultiplier());
                        currentRanker.addPoints(currentRanker.getPower());


                        for (int j = i - 1; j >= 0; j--) {                                                      //      O(r/2) worst case; probably more of O(1)
                            // If one of the already calculated Rankers have less points than this ranker
                            // swap these in the list... This way we keep the list sorted, theoretically
                            if (currentRanker.getPoints().compareTo(rankers.get(j).getPoints()) > 0) {
                                rankers.set(j + 1, rankers.get(j));
                                rankers.get(j + 1).setRank(j + 2);
                                currentRanker.setRank(j + 1);
                                rankers.set(j, currentRanker);
                            } else {
                                break;
                            }
                        }
                    }
                }
                // Write to database here
                rankerService.updateAllRankerStats(rankers);
                ladderService.save(ladder.setSize(rankers.size()).setGrowingRankerCount(growingRankerCount));
            });
        }
    }
}

