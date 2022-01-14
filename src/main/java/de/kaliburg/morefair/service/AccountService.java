package de.kaliburg.morefair.service;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.persistence.entity.Account;
import de.kaliburg.morefair.persistence.repository.AccountRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Log4j2
public class AccountService {
    private final AccountRepository accountRepository;
    private final RankerService rankerService;

    public AccountService(AccountRepository accountRepository, RankerService rankerService) {
        this.accountRepository = accountRepository;
        this.rankerService = rankerService;
    }

    @Transactional
    public AccountDetailsDTO createNewAccount() {
        Account result = new Account(UUID.randomUUID(), "");

        try {
            rankerService.getLadderSem().acquire();
            try {
                rankerService.createNewRankerForAccountOnLadder(result, 1);
            } finally {
                rankerService.getLadderSem().release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        result = accountRepository.save(result);
        result.setUsername("Mystery Guest #" + result.getId());
        result = accountRepository.save(result);

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
