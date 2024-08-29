package de.kaliburg.morefair.game.season.services.repositories;

import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementsRepository extends JpaRepository<AchievementsEntity, Long> {

  Optional<AchievementsEntity> findFirstByAccountIdAndSeasonId(
      @NonNull Long accountId,
      @NonNull Long seasonId
  );
}
