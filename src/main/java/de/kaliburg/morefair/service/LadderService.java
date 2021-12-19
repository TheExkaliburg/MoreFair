package de.kaliburg.morefair.service;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.exceptions.InvalidArgumentsException;
import de.kaliburg.morefair.repository.LadderRepository;
import de.kaliburg.morefair.repository.RankerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LadderService
{
    @PostConstruct
    public void init(){
        if(ladderRepository.findByNumber(1) == null)
            ladderRepository.save(new Ladder(UUID.randomUUID(), 1));
    }

    private final LadderRepository ladderRepository;

    public LadderService(LadderRepository ladderRepository)
    {
        this.ladderRepository = ladderRepository;
    }

    public List<Ladder> findAllLadders(){
        return ladderRepository.findAll();
    }
}
