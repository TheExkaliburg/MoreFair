package de.kaliburg.morefair.game.round.services.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.dto.RoundDto;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoundMapperTest {

  @ParameterizedTest
  @CsvSource({"0, 1", "1, 1", "7, 7", "10, 10"})
  void mapToRoundDto_usesHighestReachedNumberOrStartingLadder(
      int reachedNumber, int expectedTopLadder) {
    RoundEntity round = RoundEntity.builder()
        .id(1L)
        .seasonId(1L)
        .number(1)
        .assholeLadderNumber(9)
        .basePointsRequirement(BigInteger.valueOf(1000))
        .percentageOfAdditionalAssholes(0.5f)
        .types(EnumSet.of(RoundType.DEFAULT))
        .build();
    List<LadderEntity> ladders = new ArrayList<>();
    for (int number = 10; number >= 1; number--) {
      LadderEntity ladder = new LadderEntity().setNumber(number);
      if (number == reachedNumber) {
        ladder.setFirstReachedOn(OffsetDateTime.parse("2026-10-22T14:00:00Z"));
      }
      ladders.add(ladder);
    }
    LadderService ladderService = mock(LadderService.class);
    when(ladderService.findAllByRound(round)).thenReturn(ladders);
    RoundMapper mapper = new RoundMapper(new FairConfig(), ladderService);

    RoundDto result = mapper.mapToRoundDto(round);

    assertThat(result.getTopLadder()).isEqualTo(expectedTopLadder);
  }
}
