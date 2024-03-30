package de.kaliburg.morefair.game.season.services.impl;

import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.game.season.services.repositories.AchievementsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AchievementsServiceImpl implements AchievementsService {

  private final AchievementsRepository repository;

  private final SeasonService seasonService;

  @Override
  public Optional<AchievementsEntity> findByAccountInCurrentSeason(Long accountId) {
    SeasonEntity currentSeason = seasonService.getCurrentSeason();
    return repository.findFirstByAccountIdAndSeasonId(accountId, currentSeason.getId());
  }

  @Override
  @Transactional
  public AchievementsEntity save(AchievementsEntity achievements) {
    return repository.save(achievements);
  }

  @Override
  @Transactional
  public AchievementsEntity createForAccountInCurrentSeason(Long accountId) {
    SeasonEntity currentSeason = seasonService.getCurrentSeason();

    AchievementsEntity result = AchievementsEntity.builder()
        .seasonId(currentSeason.getId())
        .accountId(accountId)
        .assholePoints(0)
        .pressedAssholeButtons(0)
        .build();

    return repository.save(result);

  }
}
