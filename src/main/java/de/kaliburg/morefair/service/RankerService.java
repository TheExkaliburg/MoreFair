package de.kaliburg.morefair.service;

import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.repository.RankerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankerService {
    private final static int LADDER_AREA_SIZE = 20;

    private final RankerRepository rankerRepository;

    public RankerService(RankerRepository rankerRepository) {
        this.rankerRepository = rankerRepository;
    }

    public List<Ranker> findAll() {
        return rankerRepository.findAll();
    }

    public LadderViewDTO findAllRankerByHighestLadderAreaAndAccount(Account account) {
        Ranker currentRanker = findHighestRankerByAccount(account);
        Ladder currentLadder = currentRanker.getLadder();

        List<Ranker> result = findAllRankerByLadderArea(currentRanker, currentLadder);
        return new LadderViewDTO(result, currentLadder);
    }

    public List<Ranker> findByAccount(Account account) {
        return rankerRepository.findByAccount(account);
    }

    public Ranker findHighestRankerByAccount(Account account) {
        List<Ranker> temp = rankerRepository.findHighestByAccount(account);
        return (temp.size() == 0) ? null : temp.get(0);
    }

    public List<Ranker> findAllRankerByLadder(Ladder ladder) {
        return rankerRepository.findAllRankerByLadder(ladder);
    }

    public List<Ranker> findAllRankerByLadderOrderedByPoints(Ladder ladder) {
        return rankerRepository.findAllRankerByLadderOrderedByPoints(ladder);
    }

    public List<Ranker> findAllRankerByLadderArea(Ranker ranker, Ladder ladder) {
        List<Ranker> results = findAllRankerByLadder(ladder);
        results.removeIf(r -> Math.abs(ranker.getRank() - r.getRank()) > LADDER_AREA_SIZE / 2);
        return results;
    }

    @Transactional
    public void updateAllRankerStats(List<Ranker> rankers) {
        synchronized (rankerRepository) {
            rankers.forEach(ranker -> {
                rankerRepository.updateRankerStatsById(ranker.getId(), ranker.getPoints(), ranker.getPower());
            });
        }
    }
}
