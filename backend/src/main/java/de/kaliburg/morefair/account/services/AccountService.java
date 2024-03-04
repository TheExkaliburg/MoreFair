package de.kaliburg.morefair.account.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {

  AccountEntity find(UUID uuid);

  AccountEntity find(Long id);

  AccountEntity findBroadcaster();

  AccountEntity findByUsername(String username);

  AccountEntity create(String username, String password, Integer ip, boolean isGuest);

  AccountEntity save(AccountEntity account);

  List<AccountEntity> findByDisplayName(String name);

  List<AccountEntity> searchByIp(Integer lastIp);
}
