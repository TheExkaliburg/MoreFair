package de.kaliburg.morefair.game.ranker;

import de.kaliburg.morefair.account.AccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerRepository extends JpaRepository<RankerEntity, Long> {

  Optional<RankerEntity> findFirstByAccountAndGrowingIsTrueOrderByLadder_Round_NumberDescLadder_NumberDesc(
      AccountEntity account);

  @Query("select r from RankerEntity r where r.uuid = :uuid")
  Optional<RankerEntity> findByUuid(@Param("uuid") UUID uuid);


}
