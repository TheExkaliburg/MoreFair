package de.kaliburg.morefair.account.service;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.events.AccountServiceEvent;
import de.kaliburg.morefair.account.repository.AccountRepository;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.events.Event;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Service
@Log4j2
public class AccountService {
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Getter
    private List<Event> modEventList = new ArrayList<>();
    @Getter
    private Semaphore modEventSem = new Semaphore(1);

    public AccountService(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    public AccountDetailsDTO createNewAccount() {
        Account result = new Account(UUID.randomUUID(), "");
        result = saveAccount(result);
        result.setUsername("Mystery Guest #" + result.getId());
        result = saveAccount(result);

        eventPublisher.publishEvent(new AccountServiceEvent(this, result, AccountServiceEvent.AccountServiceEventType.CREATE));

        result = accountRepository.findByUuid(result.getUuid());
        log.info("Created a new Account with the uuid {} (#{}).", result.getUuid().toString(), result.getId());
        return result.convertToDTO();
    }

    @Transactional
    public Account saveAccount(Account account) {
        Account result = accountRepository.save(account);
        eventPublisher.publishEvent(new AccountServiceEvent(this, result, AccountServiceEvent.AccountServiceEventType.UPDATE));
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
        saveAccount(account);
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

    public void addModEvent(Event event) {
        try {
            modEventSem.acquire();
            try {
                modEventList.add(event);
            } finally {
                modEventSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void ban(long accountId, Event e) {
        Account account = findAccountById(accountId);
        if (account != null && !account.getAccessRole().equals(AccountAccessRole.OWNER)) {
            account.setAccessRole(AccountAccessRole.BANNED_PLAYER);
            saveAccount(account);
        }
    }

    public void mute(long accountId, Event e) {
        Account account = findAccountById(accountId);
        if (account != null && !account.getAccessRole().equals(AccountAccessRole.OWNER)) {
            account.setAccessRole(AccountAccessRole.MUTED_PLAYER);
            saveAccount(account);
        }
    }

    public void free(long accountId, Event e) {
        Account account = findAccountById(accountId);
        if (account != null && !account.getAccessRole().equals(AccountAccessRole.OWNER)) {
            account.setAccessRole(AccountAccessRole.PLAYER);
            saveAccount(account);
        }
    }

    public void resetEvents() {
        modEventList.clear();
    }

    public void mod(Long accountId, Event e) {
        Account account = findAccountById(accountId);
        if (account != null && !account.getAccessRole().equals(AccountAccessRole.OWNER)) {
            account.setAccessRole(AccountAccessRole.MODERATOR);
            saveAccount(account);
        }
    }
}
