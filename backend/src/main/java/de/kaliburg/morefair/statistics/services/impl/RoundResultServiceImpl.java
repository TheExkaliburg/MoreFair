package de.kaliburg.morefair.statistics.services.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto;
import de.kaliburg.morefair.statistics.services.RoundResultService;
import de.kaliburg.morefair.statistics.services.mapper.RoundResultMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * This Service is responsible for fetching the RoundResults after a round has ended.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoundResultServiceImpl implements RoundResultService {

  private final RoundService roundService;

  private final SeasonService seasonService;

  private final RoundResultMapper roundResultMapper;

  private final Cache<Integer, RoundResultsDto> roundResultsCache = Caffeine.newBuilder()
      .expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10)
      .build();
  ;

  public RoundResultsDto getRoundResults(Integer number) {
    RoundResultsDto result = roundResultsCache.getIfPresent(number);
    if (result == null) {
      SeasonEntity currentSeason = seasonService.getCurrentSeason();
      result = roundService.findBySeasonAndNumber(currentSeason, number)
          .map(roundResultMapper::mapToRoundResults)
          .orElse(null);

      if (result != null) {
        roundResultsCache.put(number, result);
      }
    }
    return result;
  }


}
