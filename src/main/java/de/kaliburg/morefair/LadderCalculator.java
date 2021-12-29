package de.kaliburg.morefair;

import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.repository.AccountRepository;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Log4j2
@Component
@EnableScheduling
public class LadderCalculator {
    private final LadderService ladderService;
    private final RankerService rankerService;
    private final AccountRepository accountService;

    public LadderCalculator(LadderService ladderService, RankerService rankerService, AccountRepository accountService) {
        this.ladderService = ladderService;
        this.rankerService = rankerService;
        this.accountService = accountService;
    }

    @PostConstruct
    public void init() {

    }

    // TODO: Change the algorithm, that it only needs to read from the DB when theres an update to one of the Rankers
    //  That way i would get rid of the O(r log r);
    // O(l * (r log r + (r * r/2) + r)) = O ( l * r * r)
    @Scheduled(fixedRate = 1000)
    public void calc() {
        List<Ladder> ladders = ladderService.findAllLadders();

        for (Ladder ladder : ladders) {                                                                 //O(l)
            // Read from DB here
            List<Ranker> rankers = rankerService.findAllRankerByLadderOrderedByPoints(ladder);          //  O(r log r)
            for (int i = 0; i < rankers.size(); i++) {                                                      //  O(r)
                Ranker currentRanker = rankers.get(i);
                if (currentRanker.isGrowing()) {
                    currentRanker.addPower((i + currentRanker.getBias()) * currentRanker.getMultiplier());
                    currentRanker.addPoints(currentRanker.getPower());
                    int newIndex = i;
                    for (int j = i - 1; j >= 0; j--) {                                                      //      O(r/2) worst case; probably more of O(1)
                        // If one of the already calculated Rankers have less points than this ranker
                        // swap these in the list... This way we keep the list sorted, theoretically
                        if (currentRanker.getPoints() > rankers.get(j).getPoints()) {
                            newIndex = j;
                        } else {
                            break;
                        }
                    }
                    // Swap here for resorting
                    if (newIndex != i) {
                        rankers.get(newIndex).setRank(i + 1);
                        rankers.set(i, rankers.get(newIndex));
                        currentRanker.setRank(newIndex + 1);
                        rankers.set(newIndex, currentRanker);

                    }
                }
            }

            // Write to database here
            rankerService.updateAllRankerStats(rankers);
        }
    }
}
