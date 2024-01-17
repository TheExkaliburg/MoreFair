package de.kaliburg.morefair.game.season.services;

import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.repositories.SeasonRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The Service for handling a Season. This includes Creating/Saving/Querying the Entities for the
 * Database.
 */
@RequiredArgsConstructor
@Service
public class SeasonService {

  private final SeasonRepository seasonRepository;
  private SeasonEntity currentSeason;

  /**
   * Find the Newest Season and closes and open seasons until only the newest one is left.
   *
   * @return the newest season
   */
  public SeasonEntity findNewestSeasonAndCloseOpenOnes() {
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
  }


  public Optional<SeasonEntity> find(Long id) {
    return seasonRepository.findById(id);
  }

  private SeasonEntity createNewSeason() {
    SeasonEntity seasonEntity = SeasonEntity.builder().build();
    return seasonRepository.save(seasonEntity);
  }

}
