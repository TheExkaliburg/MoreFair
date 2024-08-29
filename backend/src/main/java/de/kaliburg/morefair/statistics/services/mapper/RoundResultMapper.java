package de.kaliburg.morefair.statistics.services.mapper;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.mapper.RankerMapper;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.statistics.model.dto.LadderResultsDto;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto;
import de.kaliburg.morefair.statistics.model.dto.RoundResultsDto.RoundResultsDtoBuilder;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert{@link RoundEntity RoundEntities} and
 * {@link LadderEntity LadderEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class RoundResultMapper {

  private final RankerService rankerService;
  private final RankerMapper rankerMapper;
  private final LadderService ladderService;
  private final SeasonService seasonService;

  /**
   * Mapping a {@link RoundEntity} to a {@link RoundResultsDto}.
   *
   * @param round the {@link RoundEntity}
   * @return the {@link RoundResultsDto}
   */
  public RoundResultsDto mapToRoundResults(RoundEntity round) {
    Map<Integer, LadderEntity> ladders = ladderService.findAllByRound(round).stream()
        .collect(Collectors.toMap(LadderEntity::getNumber, Function.identity()));

    Map<Integer, LadderResultsDto> ladderResultsDtoMap = new HashMap<>();
    ladders.forEach((integer, ladder) -> {
      ladderResultsDtoMap.put(integer, mapToLadderResults(ladder));
    });

    SeasonEntity season = seasonService.findById(round.getSeasonId()).orElseThrow();

    RoundResultsDtoBuilder builder = RoundResultsDto.builder()
        .number(round.getNumber())
        .seasonNumber(season.getNumber())
        .roundTypes(round.getTypes())
        .basePointsToPromote(round.getBasePointsRequirement().toString())
        .ladders(ladderResultsDtoMap)
        .createdOn(
            round.getCreatedOn().atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME)
        );

    if (round.isClosed()) {
      builder.closedOn(
          round.getClosedOn().atZoneSameInstant(ZoneOffset.UTC)
              .format(DateTimeFormatter.ISO_DATE_TIME)
      );
    }

    return builder.build();
  }

  /**
   * Mapping a {@link LadderEntity} to a {@link LadderResultsDto}.
   *
   * @param ladder the {@link LadderEntity}
   * @return the {@link LadderResultsDto}
   */
  public LadderResultsDto mapToLadderResults(LadderEntity ladder) {
    return LadderResultsDto.builder()
        .basePointsToPromote(ladder.getBasePointsToPromote().toString())
        .ladderTypes(ladder.getTypes())
        .createdOn(ladder.getCreatedOn().atZoneSameInstant(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_DATE_TIME)
        )
        .rankers(
            rankerService.findAllByLadderId(ladder.getId()).stream()
                .map(rankerMapper::mapToPrivateDto)
                .collect(Collectors.toList())
        )
        .build();
  }

}
