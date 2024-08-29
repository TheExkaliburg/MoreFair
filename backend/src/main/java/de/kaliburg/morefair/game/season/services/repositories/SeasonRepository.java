package de.kaliburg.morefair.game.season.services.repositories;

import de.kaliburg.morefair.game.season.model.SeasonEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface SeasonRepository extends JpaRepository<SeasonEntity, Long> {


  @Query(value = "SELECT * FROM season WHERE closed_on IS NULL ORDER BY created_on DESC",
      nativeQuery = true)
  List<SeasonEntity> findAllOpenSeasonsOrderedByNewestFirst();

  @Query(value = "SELECT * FROM season WHERE closed_on IS NULL ORDER BY created_on LIMIT 1",
      nativeQuery = true)
  Optional<SeasonEntity> findOldestOpenSeason();

  @Query(value = "SELECT * FROM season ORDER BY created_on DESC LIMIT 1", nativeQuery = true)
  Optional<SeasonEntity> findNewestSeason();

  @Query(value = "SELECT * FROM season WHERE number = :number", nativeQuery = true)
  Optional<SeasonEntity> findByNumber(@Param("number") int number);
}
