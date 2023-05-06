package de.kaliburg.morefair.account;

import de.kaliburg.morefair.api.websockets.UserPrincipal;
import de.kaliburg.morefair.game.round.RoundEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This Service handles all the accounts.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final PasswordEncoder passwordEncoder;
  private AccountEntity broadcasterAccount;

  /**
   * Creates and saves a new account.
   *
   * @return the account
   */
  public AccountEntity create(UserPrincipal principal, RoundEntity currentRound) {
    AccountEntity result = new AccountEntity();

    if (principal != null) {
      result.setLastIp(principal.getIpAddress());
    }
    result.setPassword(passwordEncoder.encode(result.getUuid().toString()));

    result = save(result);

    log.debug("Created Mystery Guest (#{})", result.getId());
    return result;
  }

  @Transactional
  public AccountEntity save(AccountEntity account) {
    if (account.getPassword() == null) {
      account.setPassword(passwordEncoder.encode(account.getUuid().toString()));
    }
    AccountEntity result = accountRepository.save(account);
    eventPublisher.publishEvent(new AccountServiceEvent(this, List.of(result)));
    return result;
  }

  /**
   * Tracks the last login of an account and saves the corresponding data.
   *
   * @param account   the account
   * @param principal the principal that contains the ip-address
   * @return the updated account
   */
  public AccountEntity login(AccountEntity account, UserPrincipal principal) {
    account.setLastLogin(OffsetDateTime.now(ZoneOffset.UTC));
    account.setLastIp(principal.getIpAddress());
    return save(account);
  }

  public AccountEntity find(Long id) {
    AccountEntity result = accountRepository.findById(id).orElse(null);
    return result;
  }

  public AccountEntity find(UUID uuid) {
    return accountRepository.findByUuid(uuid).orElse(null);
  }

  public AccountEntity find(AccountEntity account) {
    return find(account.getId());
  }

  public List<AccountEntity> findByUsername(String username) {
    return accountRepository.findTop100ByUsernameContainsIgnoreCaseOrderByLastLoginDesc(username);
  }

  public AccountEntity findBroadcaster() {
    if (broadcasterAccount == null) {
      List<AccountEntity> result =
          accountRepository.findByAccessRoleOrderByIdAsc(AccountAccessRole.BROADCASTER);
      if (result.isEmpty()) {
        return null;
      }

      broadcasterAccount = result.get(0);
    }

    return broadcasterAccount;
  }

  @Transactional
  public List<AccountEntity> save(List<AccountEntity> accounts) {
    accounts.forEach(account -> {
      if (account.getPassword() == null) {
        account.setPassword(passwordEncoder.encode(account.getUuid().toString()));
      }
    });

    List<AccountEntity> result = accountRepository.saveAll(accounts);
    eventPublisher.publishEvent(new AccountServiceEvent(this, result));
    return result;
  }

  public List<AccountEntity> searchByIp(Integer ip) {
    return accountRepository.findTop100ByLastIpOrderByLastLoginDesc(ip);
  }
}
