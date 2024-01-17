package de.kaliburg.morefair.game.ranker.services.repositories;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerRepository extends JpaRepository<RankerEntity, Long> {

  @Query("select r from RankerEntity r where r.uuid = :uuid")
  Optional<RankerEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select ranker from RankerEntity ranker, RoundEntity round where ranker.accountId = :account and ranker.ladderId.round = :round")
  List<RankerEntity> findByAccountAndLadder_Round(
      @Param("account") AccountEntity account, @Param("round") RoundEntity round);

  @Query("select r from RankerEntity r where r.accountId = :account and r.ladderId.round = :round and r.growing = true")
  List<RankerEntity> findByAccountAndLadder_RoundAndGrowingIsTrue(
      @Param("account") AccountEntity account, @Param("round") RoundEntity round);


}
