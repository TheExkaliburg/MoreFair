package de.kaliburg.morefair.service;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.dto.EventDTO;
import de.kaliburg.morefair.persistence.entity.Account;
import de.kaliburg.morefair.persistence.entity.Ranker;
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


    public AccountDetailsDTO createNewAccount() {
        Account result = new Account(UUID.randomUUID(), "");
        result = saveAccount(result);
        result.setUsername("Mystery Guest #" + result.getId());
        result = saveAccount(result);
        result = accountRepository.findByUuid(result.getUuid());

        try {
            rankerService.getLadderSem().acquire();
            try {
                Ranker ranker = rankerService.createNewRankerForAccountOnLadder(result, 1);
                result.getRankers().add(ranker);
            } finally {
                rankerService.getLadderSem().release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        result = accountRepository.findByUuid(result.getUuid());
        log.info("Created a new Account with the uuid {} (#{}).", result.getUuid().toString(), result.getId());
        return result.convertToDTO();
    }

    @Transactional
    protected Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account findAccountByUUID(UUID uuid) {
        return accountRepository.findByUuid(uuid);
    }

    @Transactional
    public void updateUsername(Account account, String username) {
        account.setUsername(username);
        accountRepository.save(account);
        EventDTO event = new EventDTO(EventDTO.EventType.NAMECHANGE, account.getId());
        event.setChangedUsername(username);
        rankerService.addGlobalEvent(event);
    }

    public void login(Account account) {
        // Set Login Date
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);
    }

    public Integer findMaxTimeAsshole() {
        return (accountRepository.findTopByTimesAsshole() != null) ? accountRepository.findTopByTimesAsshole() : 0;
    }

    public Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId).get();
    }
}
