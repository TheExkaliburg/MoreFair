package de.kaliburg.morefair.service;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.repository.AccountRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService
{
    @Getter
    private List<Account> users;
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository)
    {
        users = new ArrayList<Account>();
        users.add(new Account(UUID.randomUUID(), "Kaliburg"));
        users.add(new Account(UUID.randomUUID(), "Tobi"));
        users.add(new Account(UUID.randomUUID(), "Mystery Guest 7"));
        this.accountRepository = accountRepository;
    }

    public Account saveUser(Account user){
        users.add(user);

        return user;
    }
}
