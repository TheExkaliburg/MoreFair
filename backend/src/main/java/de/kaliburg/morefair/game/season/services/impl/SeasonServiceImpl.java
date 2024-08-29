package de.kaliburg.morefair.game.season.services.impl;

import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.game.season.services.repositories.SeasonRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
      List<SeasonEntity> openSeasons = seasonRepository.findAllOpenSeasonsOrderedByNewestFirst();

      if (openSeasons.isEmpty()) {
        currentSeason = createNewSeason();
        return currentSeason;
      }

      // Remove all OpenSeasons until only one is left over
      while (openSeasons.size() > 1) {
        SeasonEntity oldestOpenSeason = openSeasons.remove(openSeasons.size() - 1);
        oldestOpenSeason.setClosedOn(OffsetDateTime.now(ZoneOffset.UTC));
        seasonRepository.save(oldestOpenSeason);
      }

      currentSeason = openSeasons.get(openSeasons.size() - 1);
      return currentSeason;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional
  public SeasonEntity getCurrentSeason() {
    try (var ignored = semaphore.enter()) {
      if (currentSeason == null) {
        currentSeason = seasonRepository.findOldestOpenSeason().orElseGet(this::createNewSeason);
      }

      return currentSeason;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<SeasonEntity> findById(long id) {
    return seasonRepository.findById(id);
  }

  @Override
  public Optional<SeasonEntity> findByNumber(int number) {
    if (currentSeason != null && currentSeason.getNumber() == number) {
      return Optional.of(currentSeason);
    }

    return seasonRepository.findByNumber(number);
  }

  private SeasonEntity createNewSeason() {
    Integer oldNumber = seasonRepository.findNewestSeason()
        .map(SeasonEntity::getNumber)
        .orElse(0);

    SeasonEntity seasonEntity = SeasonEntity.builder()
        .number(oldNumber + 1)
        .build();
    return seasonRepository.save(seasonEntity);
  }

}
