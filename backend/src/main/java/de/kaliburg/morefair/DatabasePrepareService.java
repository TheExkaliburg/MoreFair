package de.kaliburg.morefair;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabasePrepareService {

  private final AccountRepository accountRepository;

  private final PasswordEncoder passwordEncoder;

  @Scheduled(fixedRate = 1000 * 60)
  public void prepareDatabase() {
    List<AccountEntity> top10ByPasswordIsNull = accountRepository.findTop10ByPasswordIsNull();
    log.info("Preparing the passwords of {} accounts", top10ByPasswordIsNull.size());
    top10ByPasswordIsNull.forEach(account -> {
      account.setPassword(passwordEncoder.encode(account.getUuid().toString()));
      accountRepository.save(account);
    });
    log.info("Prepared the password for {} accounts", top10ByPasswordIsNull.size());
  }

}
