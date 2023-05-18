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

  @Query("SELECT a FROM AccountEntity a WHERE a.uuid = :uuid")
  Optional<AccountEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select a from AccountEntity a where lower( a.username) like concat('%', lower(:username), '%')")
  List<AccountEntity> findAccountsByUsernameIsContaining(
      @Param("username") @NonNull String username);

  @Query("select a from AccountEntity a where a.accessRole = :accessRole order by a.id")
  List<AccountEntity> findByAccessRoleOrderByIdAsc(
      @Param("accessRole") AccountAccessRole accessRole);

  List<AccountEntity> findTop100ByUsernameContainsIgnoreCaseOrderByLastLoginDesc(
      @Param("username") @NonNull String username);

  List<AccountEntity> findTop100ByLastIpOrderByLastLoginDesc(
      @Param("lastIp") Integer lastIp);


  List<AccountEntity> findTop100ByPasswordIsNull();


}
