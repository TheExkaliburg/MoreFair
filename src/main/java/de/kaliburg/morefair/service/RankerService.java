package de.kaliburg.morefair.service;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.exceptions.InvalidArgumentsException;
import de.kaliburg.morefair.repository.RankerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankerService
{
    private final static int LADDER_AREA_SIZE = 20;

    private final RankerRepository rankerRepository;

    public RankerService(RankerRepository rankerRepository)
    {
        this.rankerRepository = rankerRepository;
    }

    public List<Ranker> findAll(){
        return rankerRepository.findAll();
    }

    public List<Ranker> findAllRankerForHighestLadderAreaForAccount(Account account) {
        if(account == null) {
            throw new InvalidArgumentsException("account should not be empty");
        }

        Ranker currentRanker = findHighestRankerByAccount(account);
        Ladder currentLadder = currentRanker.getLadder();

        return findAllRankerForLadderArea(currentRanker, currentLadder);
    }

    public List<Ranker> findByAccount(Account account){
        return rankerRepository.findByAccount(account);
    }

    public Ranker findHighestRankerByAccount(Account account){
        return rankerRepository.findHighestByAccount(account);
    }

    public List<Ranker> findAllRankerForLadder(Ladder ladder){
        return rankerRepository.findAllRankerForLadder(ladder);
    }

    public List<Ranker> findAllRankerForLadderArea(Ranker ranker, Ladder ladder){
        List<Ranker> results = findAllRankerForLadder(ladder);
        results.removeIf(r -> Math.abs(ranker.getPosition() - r.getPosition()) > LADDER_AREA_SIZE / 2);
        return results;
    }
}
