package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.account.AccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerRepository extends JpaRepository<RankerEntity, Long> {

  Optional<RankerEntity> findFirstByAccountAndLadder_RoundAndGrowingIsTrueOrderByLadder_NumberDesc(
      AccountEntity account, RoundEntity round);

  @Query("select r from RankerEntity r where r.uuid = :uuid")
  Optional<RankerEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select r from RankerEntity r where r.account = :account and r.ladder.round = :round")
  List<RankerEntity> findByAccountAndLadder_Round(
      @Param("account") AccountEntity account, @Param("round") RoundEntity round);

  @Query("select r from RankerEntity r where r.account = :account and r.ladder.round = :round and r.growing = true")
  List<RankerEntity> findByAccountAndLadder_RoundAndGrowingIsTrue(
      @Param("account") AccountEntity account, @Param("round") RoundEntity round);


}
