package de.kaliburg.morefair.game.vinegar.services.repositories;

import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VinegarThrowRepository extends JpaRepository<VinegarThrowEntity, Long> {

  int THROWS_PER_PAGE = 50;

  @Query(value = "SELECT vt.* FROM vinegar_throw vt "
      + "WHERE vt.thrower_account_id = :accountId "
      + "OR vt.target_account_id = :accountId "
      + "ORDER BY vt.timestamp DESC", nativeQuery = true)
  List<VinegarThrowEntity> findAllByAccountId(@Param("accountId") Long accountId,
      Pageable pageable);

  default List<VinegarThrowEntity> findAllByAccountId(Long accountId) {
    return findAllByAccountId(accountId, PageRequest.of(0, THROWS_PER_PAGE));
  }

  @Query(value = "SELECT vt.* FROM vinegar_throw vt "
      + "JOIN ladder l ON l.id = vt.ladder_id "
      + "WHERE l.round_id = :roundId "
      + "AND (vt.thrower_account_id = :accountId "
      + "OR vt.target_account_id = :accountId) "
      + "ORDER BY vt.timestamp DESC", nativeQuery = true)
  List<VinegarThrowEntity> findAllByAccountIdAndRoundId(@Param("accountId") Long accountId,
      @Param("roundId") Long roundId);

}
