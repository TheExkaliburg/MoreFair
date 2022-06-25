package de.kaliburg.morefair.game.round;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LadderRepository extends JpaRepository<LadderEntity, Long> {

  @Query("select l from LadderEntity l where l.uuid = :uuid")
  Optional<LadderEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select l from LadderEntity l where l.round = :round and l.number = :number")
  Optional<LadderEntity> findByRoundAndNumber(
      @Param("round") RoundEntity round, @Param("number") Integer number);

  @Query("select l from LadderEntity l where l.round = :round order by l.number")
  List<LadderEntity> findByRoundOrderByNumberAsc(@Param("round") RoundEntity round);
}
