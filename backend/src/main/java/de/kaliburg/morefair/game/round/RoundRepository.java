package de.kaliburg.morefair.game.round;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {

  @Query("select r from RoundEntity r where r.number = :number")
  Optional<RoundEntity> findByNumber(@Param("number") Integer number);


}
