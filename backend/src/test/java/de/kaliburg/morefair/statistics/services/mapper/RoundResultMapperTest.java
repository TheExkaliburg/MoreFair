package de.kaliburg.morefair.statistics.services.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.mapper.RankerMapper;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.statistics.model.dto.LadderResultsDto;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoundResultMapperTest {

  @ParameterizedTest
  @MethodSource("firstReachedTimes")
  void mapToLadderResults_preservesNullableFirstReachedOn(
      OffsetDateTime firstReachedOn, String expectedTimestamp) {
    RankerService rankerService = mock(RankerService.class);
    when(rankerService.findAllByLadderId(1L)).thenReturn(List.of());
    RoundResultMapper mapper = new RoundResultMapper(rankerService, mock(RankerMapper.class),
        mock(LadderService.class), mock(SeasonService.class));
    LadderEntity ladder = LadderEntity.builder()
        .id(1L)
        .number(1)
        .roundId(1L)
        .scaling(1)
        .basePointsToPromote(BigInteger.ZERO)
        .firstReachedOn(firstReachedOn)
        .build();

    LadderResultsDto result = mapper.mapToLadderResults(ladder);

    assertThat(result.getFirstReachedOn()).isEqualTo(expectedTimestamp);
  }

  private static List<Arguments> firstReachedTimes() {
    return List.of(
        Arguments.of(null, null),
        Arguments.of(OffsetDateTime.parse("2026-10-22T14:00:00Z"), "2026-10-22T14:00:00Z"),
        Arguments.of(OffsetDateTime.parse("2026-10-22T16:00:00+02:00"), "2026-10-22T14:00:00Z")
    );
  }
}
