package de.kaliburg.morefair.account.repository;

import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.rankers WHERE a.uuid = :uuid") AccountEntity findByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT MAX(a.timesAsshole) FROM AccountEntity a")
    Integer findMaxTimesAsshole();

    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.rankers")
    Set<AccountEntity> findAllAccountsJoinedWithRankers();

    @Query("SELECT a FROM AccountEntity a WHERE a.accessRole = :role")
    List<AccountEntity> findAllAccountsByAccessRole(@Param("role") AccountAccessRole accessRole);

    @Query("select a from AccountEntity a where lower( a.username) like concat('%', lower(:username), '%')")
    List<AccountEntity> findAccountsByUsernameIsContaining(@Param("username") @NonNull String username);
}
