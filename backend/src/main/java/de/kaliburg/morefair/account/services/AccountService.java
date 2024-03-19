package de.kaliburg.morefair.account.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {

  Optional<AccountEntity> findByUuid(UUID uuid);

  Optional<AccountEntity> findById(Long id);

  Optional<AccountEntity> findBroadcaster();

  Optional<AccountEntity> findByUsername(String username);

  Optional<AccountEntity> create(String username, String password, Integer ip, boolean isGuest);

  List<AccountEntity> findByDisplayName(String name);

  List<AccountEntity> searchByIp(Integer lastIp);

  AccountEntity save(AccountEntity account);

  List<AccountEntity> saveAll(List<AccountEntity> accounts);
}
