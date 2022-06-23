package de.kaliburg.morefair.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

  @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.rankers WHERE a.uuid = :uuid")
  Optional<AccountEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select a from AccountEntity a where lower( a.username) like concat('%', lower(:username), '%')")
  List<AccountEntity> findAccountsByUsernameIsContaining(
      @Param("username") @NonNull String username);
}
