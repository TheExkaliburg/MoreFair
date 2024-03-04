package de.kaliburg.morefair.game.season.services;

import de.kaliburg.morefair.game.season.model.SeasonEntity;
import java.util.Optional;

/**
 * The Service for handling a Season. Should be able to find the Newest Season.
 */
public interface SeasonService {


  SeasonEntity findNewestSeason();

  Optional<SeasonEntity> findById(long id);

  SeasonEntity getCurrentSeason();
}
