package de.kaliburg.morefair.service;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.repository.AccountRepository;
import de.kaliburg.morefair.repository.LadderRepository;
import de.kaliburg.morefair.repository.RankerRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Log4j2
public class AccountService {
    private final AccountRepository accountRepository;
    private final LadderRepository ladderRepository;
    private final RankerRepository rankerRepository;

    public AccountService(AccountRepository accountRepository, LadderRepository ladderRepository,
                          RankerRepository rankerRepository) {
        this.accountRepository = accountRepository;
        this.ladderRepository = ladderRepository;
        this.rankerRepository = rankerRepository;
    }

    @Transactional
    public AccountDetailsDTO createNewAccount() {
        Account result = new Account(UUID.randomUUID(), "");
        Ladder l1 = ladderRepository.findByNumber(1);
        Integer rank = l1.getSize() + 1;
        Ranker ranker = new Ranker(UUID.randomUUID(), l1, result, rank);

        result = accountRepository.save(result);
        result.setUsername("Mystery Guest " + result.getId());
        result = accountRepository.save(result);

        l1.setSize(l1.getSize() + 1);
        l1.setGrowingRankerCount(l1.getGrowingRankerCount() + 1);
        ladderRepository.save(l1);

        rankerRepository.save(ranker);

        log.info("Created a new Account with the uuid {} (#{}).", result.getUuid().toString(), result.getId());
        return result.convertToDTO();
    }

    public Account findAccountByUUID(UUID uuid) {
        return accountRepository.findByUuid(uuid);
    }

    public void updateUsername(Account account, String username) {
        account.setUsername(username);
        accountRepository.save(account);
    }

    public void login(Account account) {
        // Set Login Date
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);
    }

    public Integer findMaxTimeAsshole() {
        return accountRepository.findTopByTimesAsshole();
    }
}
