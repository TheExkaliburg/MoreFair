package de.kaliburg.morefair.game.round.services.repositories;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {

  @Query("select r from RoundEntity r where r.number = :number")
  Optional<RoundEntity> findByNumber(@Param("number") Integer number);

  @Query("select r from RoundEntity r where r.seasonId = :seasonId and r.number = :number")
  Optional<RoundEntity> findBySeasonAndNumber(@Param("seasonId") Long seasonId,
      @Param("number") Integer number);

  @Query("SELECT r FROM RoundEntity r WHERE r.seasonId = :seasonId and r.number = "
      + "   (SELECT MAX(r2.number) FROM RoundEntity r2 WHERE r2.seasonId = :seasonId)")
  Optional<RoundEntity> findNewestRoundOfSeason(@Param("seasonId") Long seasonId);
}
