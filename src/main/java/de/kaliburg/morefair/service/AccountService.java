package de.kaliburg.morefair.service;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.exceptions.InternalServerException;
import de.kaliburg.morefair.repository.AccountRepository;
import de.kaliburg.morefair.repository.LadderRepository;
import de.kaliburg.morefair.repository.RankerRepository;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.flogger.Flogger;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class AccountService
{
    private final AccountRepository accountRepository;
    private final LadderRepository ladderRepository;
    private final RankerRepository rankerRepository;

    public AccountService(AccountRepository accountRepository, LadderRepository ladderRepository,
            RankerRepository rankerRepository)
    {
        this.accountRepository = accountRepository;
        this.ladderRepository = ladderRepository;
        this.rankerRepository = rankerRepository;
    }

    public Account createNewAccount(){
        UUID uuid = UUID.randomUUID();

        for(int i = 0; i < 10;i++){
            if(findAccountByUUID(uuid) == null){
                break;
            }
            uuid = UUID.randomUUID();
        }
        if(findAccountByUUID(uuid) != null)
            throw new InternalServerException("Couldn't generate free uuid");

        Account result = new Account(uuid, generateNewUsername());
        Ranker ranker = new Ranker();
        Ladder l1 = ladderRepository.findByNumber(1);

        ranker.setAccount(result);
        ranker.setLadder(l1);


        result = accountRepository.save(result);
        rankerRepository.save(ranker);
        log.info("Created a new Account with the uuid {}", result.getUuid().toString());
        return result;
    }

    public String generateNewUsername(){
        Random random = new Random();
        String result = "Mystery Guest " + random.nextInt(99999);
        return result;
    }

    public Account findAccountByUUID(UUID uuid){
        return accountRepository.findByUUID(uuid);
    }
}
