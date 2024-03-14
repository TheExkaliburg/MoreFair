package de.kaliburg.morefair.game.season.services.impl;

import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.game.season.services.repositories.SeasonRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Service for handling a Season. This includes Creating/Saving/Querying the Entities for the
 * Database.
 */
@RequiredArgsConstructor
@Service
public class SeasonServiceImpl implements SeasonService {

  private final CriticalRegion semaphore = new CriticalRegion(1);

  private final SeasonRepository seasonRepository;
  private SeasonEntity currentSeason;

  /**
   * Find the Newest Season and closes and open seasons until only the newest one is left.
   *
   * @return the newest season
   */
  @Override
  @Transactional
  public SeasonEntity findNewestSeason() {
    try (var ignored = semaphore.enter()) {
      if (currentSeason == null) {
        List<SeasonEntity> openSeasons = seasonRepository.findAllOpenSeasonsOrderedByNewestFirst();

        if (openSeasons.isEmpty()) {
          currentSeason = createNewSeason();
          return currentSeason;
        }

        // Remove all OpenSeasons until only one is left over
        while (openSeasons.size() > 1) {
          SeasonEntity oldestOpenSeason = openSeasons.remove(openSeasons.size() - 1);
          oldestOpenSeason.setClosedOn(OffsetDateTime.now());
          seasonRepository.save(oldestOpenSeason);
        }

        currentSeason = openSeasons.get(openSeasons.size() - 1);
      }

      return currentSeason;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional
  public SeasonEntity getCurrentSeason() {
    try (var ignored = semaphore.enter()) {
      if (currentSeason == null) {
        currentSeason = seasonRepository.findOldestOpenSeason().orElse(createNewSeason());
      }

      return currentSeason;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<SeasonEntity> findById(long id) {
    return seasonRepository.findById(id);
  }

  private SeasonEntity createNewSeason() {
    SeasonEntity seasonEntity = SeasonEntity.builder().build();
    return seasonRepository.save(seasonEntity);
  }

}
