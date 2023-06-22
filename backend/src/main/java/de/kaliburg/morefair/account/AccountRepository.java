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

  @Query("select a from AccountEntity a where LOWER(a.username) = LOWER(:username)")
  Optional<AccountEntity> findByUsername(@Param("username") String username);

  @Query("SELECT a FROM AccountEntity a WHERE a.uuid = :uuid")
  Optional<AccountEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select a from AccountEntity a where lower( a.displayName) like concat('%', lower(:displayName), '%')")
  List<AccountEntity> findAccountsByDisplayNameIsContaining(
      @Param("displayName") @NonNull String displayName);

  @Query("select a from AccountEntity a where a.accessRole = :accessRole order by a.id")
  List<AccountEntity> findByAccessRoleOrderByIdAsc(
      @Param("accessRole") AccountAccessRole accessRole);

  List<AccountEntity> findTop100ByDisplayNameContainsIgnoreCaseOrderByLastLoginDesc(
      @Param("displayName") @NonNull String displayName);

  List<AccountEntity> findTop100ByLastIpOrderByLastLoginDesc(
      @Param("lastIp") Integer lastIp);


}
