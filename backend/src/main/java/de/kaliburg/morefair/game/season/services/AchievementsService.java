package de.kaliburg.morefair.game.season.services;

import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import java.util.Optional;

public interface AchievementsService {

  Optional<AchievementsEntity> findByAccountInCurrentSeason(Long accountId);

  AchievementsEntity save(AchievementsEntity achievements);

  AchievementsEntity createForAccountInCurrentSeason(Long accountId);

  default AchievementsEntity findOrCreateByAccountInCurrentSeason(Long accountId) {
    return findByAccountInCurrentSeason(accountId)
        .orElseGet(() -> createForAccountInCurrentSeason(accountId));
  }
}
