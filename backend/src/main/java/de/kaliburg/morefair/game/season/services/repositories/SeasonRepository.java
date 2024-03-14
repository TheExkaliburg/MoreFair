package de.kaliburg.morefair.game.season.services.repositories;

import de.kaliburg.morefair.game.season.model.SeasonEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface SeasonRepository extends JpaRepository<SeasonEntity, Long> {


  @Query(value = "SELECT s FROM SeasonEntity s WHERE s.closedOn = null ORDER BY s.createdOn DESC ")
  List<SeasonEntity> findAllOpenSeasonsOrderedByNewestFirst();

  @Query(value = "SELECT * FROM season WHERE closed_on IS NULL ORDER BY created_on LIMIT 1",
      nativeQuery = true)
  Optional<SeasonEntity> findOldestOpenSeason();
}
