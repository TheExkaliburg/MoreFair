package de.kaliburg.morefair.account.services;

import de.kaliburg.morefair.account.model.AccountSettingsEntity;
import java.util.Optional;

public interface AccountSettingsService {

  Optional<AccountSettingsEntity> findByAccount(Long accountId);

  AccountSettingsEntity createForAccount(Long accountId);

  AccountSettingsEntity save(AccountSettingsEntity accountSettings);

  default AccountSettingsEntity findOrCreateByAccount(Long accountId) {
    return findByAccount(accountId)
        .orElseGet(() -> createForAccount(accountId));
  }

}
