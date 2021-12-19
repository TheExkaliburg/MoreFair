package de.kaliburg.morefair.service;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.exceptions.InvalidArgumentsException;
import de.kaliburg.morefair.repository.LadderRepository;
import de.kaliburg.morefair.repository.RankerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LadderService
{
    private final LadderRepository ladderRepository;

    public LadderService(LadderRepository ladderRepository)
    {
        this.ladderRepository = ladderRepository;
    }

    public List<Ladder> findAllLadders(){
        return ladderRepository.findAll();
    }
}
