package de.kaliburg.morefair.account.services.impl;

import de.kaliburg.morefair.account.model.AccountSettingsEntity;
import de.kaliburg.morefair.account.services.AccountSettingsService;
import de.kaliburg.morefair.account.services.repositories.AccountSettingsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountSettingsServiceImpl implements AccountSettingsService {

  private final AccountSettingsRepository repository;

  @Override
  public Optional<AccountSettingsEntity> findByAccount(Long accountId) {
    return repository.findFirstByAccountId(accountId);
  }

  @Override
  @Transactional
  public AccountSettingsEntity createForAccount(Long accountId) {
    var result = AccountSettingsEntity.builder()
        .accountId(accountId)
        .build();

    return repository.save(result);
  }

  @Override
  @Transactional
  public AccountSettingsEntity save(AccountSettingsEntity accountSettings) {
    return repository.save(accountSettings);
  }
}
