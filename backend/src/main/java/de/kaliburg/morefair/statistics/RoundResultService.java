package de.kaliburg.morefair.statistics;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.statistics.model.dto.LadderResultsDto;
import de.kaliburg.morefair.statistics.model.dto.LadderResultsDto.LadderResultsDtoBuilder;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto.RoundResultsDtoBuilder;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * This Service is responsible for fetching the RoundResults after a round has ended.
 */
@Service
@Slf4j
public class RoundResultService {

  private final Cache<Integer, RoundResultsDto> roundResultsCache;

  private final RoundService roundService;

  private final FairConfig config;

  public RoundResultService(RoundService roundService, FairConfig config) {
    this.roundService = roundService;
    this.config = config;
    roundResultsCache = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10)
        .build();
  }

  public RoundResultsDto getRoundResults(Integer number) {
    RoundResultsDto result = roundResultsCache.getIfPresent(number);
    if (result == null) {
      result = roundService.find(number)
          .map(this::convertToDto)
          .orElse(null);

      if (result != null) {
        roundResultsCache.put(number, result);
      }
    }
    return result;
  }


  private RoundResultsDto convertToDto(RoundEntity round) {
    Map<Integer, LadderEntity> ladders = round.getLadders().stream()
        .collect(Collectors.toMap(LadderEntity::getNumber,
            Function.identity()));
    Map<Integer, LadderResultsDto> ladderResultsDtoMap = new HashMap<>();

    ladders.forEach((integer, ladder) -> {
      ladderResultsDtoMap.put(integer, convertToDto(ladder));
    });

    RoundResultsDtoBuilder builder = RoundResultsDto.builder()
        .number(round.getNumber())
        .roundTypes(round.getTypes())
        .basePointsToPromote(round.getBasePointsRequirement().toString())
        .ladders(ladderResultsDtoMap)
        .createdOn(round.getCreatedOn().atZoneSameInstant(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_DATE_TIME));

    if (round.isClosed()) {
      builder.closedOn(round.getClosedOn().atZoneSameInstant(ZoneOffset.UTC)
          .format(DateTimeFormatter.ISO_DATE_TIME));
    }

    return builder.build();
  }

  private LadderResultsDto convertToDto(LadderEntity ladder) {
    LadderResultsDtoBuilder builder = LadderResultsDto.builder()
        .basePointsToPromote(ladder.getBasePointsToPromote().toString())
        .ladderTypes(ladder.getTypes())
        .createdOn(ladder.getCreatedOn().atZoneSameInstant(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_DATE_TIME)
        );

    // FIXME: Need to populate Rankers in this part
    //    ladder.getRankers().forEach(ranker -> {
    //      RankerPrivateDto dto = new RankerPrivateDto(ranker, config);
    //      rankers.add(dto);
    //    });
    //    rankers.sort(Comparator.comparing(RankerPrivateDto::getRank));
    return builder.build();
  }
}
