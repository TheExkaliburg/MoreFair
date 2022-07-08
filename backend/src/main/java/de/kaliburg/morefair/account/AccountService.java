package de.kaliburg.morefair.account;

import de.kaliburg.morefair.api.websockets.UserPrincipal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * This Service handles all the accounts.
 */
@Service
@Log4j2
public class AccountService {

  private final AccountRepository accountRepository;
  private final ApplicationEventPublisher eventPublisher;

  public AccountService(AccountRepository accountRepository,
      ApplicationEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Creates and saves a new account.
   *
   * @return the account
   */
  @Transactional
  public AccountEntity create(UserPrincipal principal) {
    AccountEntity result = new AccountEntity();

    if (principal != null) {
      result.setLastIp(principal.getIpAddress());
    }
    result = save(result);
    log.info("Created Mystery Guest (#{})", result.getId());
    return result;
  }

  @Transactional
  public AccountEntity save(AccountEntity account) {
    return accountRepository.save(account);
  }

  /**
   * Tracks the last login of an account and saves the corresponding data.
   *
   * @param account   the account
   * @param principal the principal that contains the ip-address
   * @return the updated account
   */
  public AccountEntity login(AccountEntity account, UserPrincipal principal) {
    account.setLastLogin(ZonedDateTime.now());
    account.setLastIp(principal.getIpAddress());
    return save(account);
  }

  public AccountEntity find(Long id) {
    return accountRepository.findById(id).orElse(null);
  }

  public AccountEntity find(UUID uuid) {
    return accountRepository.findByUuid(uuid).orElse(null);
  }

  public AccountEntity find(AccountEntity account) {
    return find(account.getId());
  }

  public List<AccountEntity> findByUsername(String username) {
    return accountRepository.findAccountsByUsernameIsContaining(username);
  }
}
