package de.kaliburg.morefair.moderation.events.services.repositories;

import de.kaliburg.morefair.moderation.events.model.NameChangeEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NameChangeRepository extends JpaRepository<NameChangeEntity, Long> {

  int NAME_CHANGES_PER_PAGE = 50;

  @Query(value = "SELECT * FROM name_change nc "
      + "WHERE nc.account_id = :accountId", nativeQuery = true)
  List<NameChangeEntity> findByAccount(@Param("accountId") Long accountId, Pageable pageable);

  default List<NameChangeEntity> findByAccount(Long accountId) {
    return findByAccount(accountId, Pageable.ofSize(NAME_CHANGES_PER_PAGE));
  }

}
