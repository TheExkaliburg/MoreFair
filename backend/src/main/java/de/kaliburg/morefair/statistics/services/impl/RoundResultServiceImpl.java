package de.kaliburg.morefair.statistics.services.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto;
import de.kaliburg.morefair.statistics.services.RoundResultService;
import de.kaliburg.morefair.statistics.services.mapper.RoundResultMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
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

  private final Cache<Pair<Integer, Integer>, RoundResultsDto> roundResultsCache = Caffeine.newBuilder()
      .expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10)
      .build();

  @Override
  public RoundResultsDto getRoundResults(Integer seasonNumber, Integer roundNumber) {
    Pair<Integer, Integer> id = Pair.with(seasonNumber, roundNumber);
    RoundResultsDto result = roundResultsCache.getIfPresent(id);
    if (result == null) {
      SeasonEntity currentSeason = seasonService.findByNumber(seasonNumber).orElse(null);
      if (currentSeason == null) {
        return null;
      }

      result = roundService.findBySeasonAndNumber(currentSeason, roundNumber)
          .map(roundResultMapper::mapToRoundResults)
          .orElse(null);

      if (result != null) {
        roundResultsCache.put(id, result);
      }
    }
    return result;
  }


}
