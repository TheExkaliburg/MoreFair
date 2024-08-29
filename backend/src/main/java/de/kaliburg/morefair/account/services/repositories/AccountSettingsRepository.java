package de.kaliburg.morefair.account.services.repositories;

import de.kaliburg.morefair.account.model.AccountSettingsEntity;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountSettingsRepository extends JpaRepository<AccountSettingsEntity, Long> {

  Optional<AccountSettingsEntity> findFirstByAccountId(@NonNull Long accountId);
}
