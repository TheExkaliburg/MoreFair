package de.kaliburg.morefair.account;

import de.kaliburg.morefair.api.websockets.UserPrincipal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This Service handles all the accounts.
 */
@Service
@Log4j2
public class AccountService implements UserDetailsService {

  private final AccountRepository accountRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final PasswordEncoder passwordEncoder;

  private AccountEntity broadcasterAccount;

  public AccountService(AccountRepository accountRepository,
      ApplicationEventPublisher eventPublisher, PasswordEncoder passwordEncoder) {
    this.accountRepository = accountRepository;
    this.eventPublisher = eventPublisher;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Creates and saves a new account.
   *
   * @return the account
   */
  public AccountEntity create(String email, String password, Integer ipAddress, boolean isGuest) {
    AccountEntity result = new AccountEntity();
    result.setUsername(email);
    result.setPassword(passwordEncoder.encode(password));
    result.setLastIp(ipAddress);
    result.setGuest(isGuest);

    result = save(result);

    log.debug("Created Mystery Guest (#{})", result.getId());
    return result;
  }

  @Transactional
  public AccountEntity save(AccountEntity account) {
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

  public List<AccountEntity> findByDisplayName(String displayName) {
    return accountRepository.findAccountsByDisplayNameIsContaining(displayName);
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
    List<AccountEntity> result = accountRepository.saveAll(accounts);
    eventPublisher.publishEvent(new AccountServiceEvent(this, result));
    return result;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AccountEntity account = accountRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(account.getAccessRole().name()));

    return new User(account.getUsername(), account.getPassword(), authorities);
  }

  public AccountEntity findByUsername(String username) {
    return accountRepository.findByUsername(username).orElse(null);
  }
}
