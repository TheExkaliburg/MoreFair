package de.kaliburg.morefair.account.service;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.events.AccountEvent;
import de.kaliburg.morefair.account.repository.AccountRepository;
import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.events.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Log4j2
public class AccountService {
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AccountService(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }


    public AccountDetailsDTO createNewAccount() {
        Account result = new Account(UUID.randomUUID(), "");
        result = saveAccount(result);
        result.setUsername("Mystery Guest #" + result.getId());
        result = saveAccount(result);
        result = accountRepository.findByUuid(result.getUuid());

        eventPublisher.publishEvent(new AccountEvent(this, result, AccountEvent.AccountEventType.CREATE));

        result = accountRepository.findByUuid(result.getUuid());
        log.info("Created a new Account with the uuid {} (#{}).", result.getUuid().toString(), result.getId());
        return result.convertToDTO();
    }

    @Transactional
    public Account saveAccount(Account account) {
        Account result = accountRepository.save(account);
        eventPublisher.publishEvent(new AccountEvent(this, result, AccountEvent.AccountEventType.UPDATE));
        return result;
    }

    public Account findAccountByUUID(UUID uuid) {
        return accountRepository.findByUuid(uuid);
    }

    public boolean updateUsername(Long accountId, Event event) {
        Account account = findAccountById(accountId);
        String newUsername = (String) event.getData();
        account.setUsername(newUsername);
        account = saveAccount(account);
        return true;
    }

    public void updateActivity(Account account) {
        // Set Login Date
        account.setLastLogin(LocalDateTime.now());
        accountRepository.save(account);
    }

    public Integer findMaxTimeAsshole() {
        return (accountRepository.findMaxTimesAsshole() != null) ? accountRepository.findMaxTimesAsshole() : 0;
    }

    public Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId).get();
    }

    public Integer findMaxTimesAsshole() {
        return accountRepository.findMaxTimesAsshole();
    }

    public Set<Account> findAllAccountsJoinedWithRankers() {
        return accountRepository.findAllAccountsJoinedWithRankers();
    }

    public Account findByUuid(UUID uuid) {
        return accountRepository.findByUuid(uuid);
    }
}
