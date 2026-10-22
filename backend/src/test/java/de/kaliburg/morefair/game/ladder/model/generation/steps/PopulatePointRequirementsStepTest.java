package de.kaliburg.morefair.game.ladder.model.generation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PopulatePointRequirementsStepTest {

  LadderGenerationStep cut = new PopulatePointRequirementsStep();

  @ParameterizedTest(name = "{0}, scaling {1}: points between {2} and {3}")
  @MethodSource("pointRequirementCases")
  void populatePoints_matchesLadderSizeAndScaling(
      List<LadderType> types, int scaling, long minimum, long maximum) {
    EnumSet<LadderType> ladderTypes = EnumSet.noneOf(LadderType.class);
    ladderTypes.addAll(types);
    LadderEntity ladder = LadderEntity.builder()
        .roundId(1L)
        .number(1)
        .scaling(scaling)
        .types(ladderTypes)
        .basePointsToPromote(BigInteger.ZERO)
        .build();

    cut.apply(List.of(ladder), getContext());

    assertThat(ladder.getBasePointsToPromote())
        .isBetween(BigInteger.valueOf(minimum), BigInteger.valueOf(maximum));
  }

  private LadderGenerationContext getContext() {
    return new LadderGenerationContext(RoundEntity.builder()
            .id(1L)
            .seasonId(1L)
            .number(1)
            .assholeLadderNumber(15)
            .basePointsRequirement(BigInteger.valueOf(1000))
            .percentageOfAdditionalAssholes(0.5f)
            .types(EnumSet.of(RoundType.DEFAULT))
            .build());
  }

  private static List<Arguments> pointRequirementCases() {
    return List.of(
        Arguments.of(List.of(LadderType.DEFAULT), 1, 800L, 1200L),
        Arguments.of(List.of(LadderType.DEFAULT), 3, 2400L, 3600L),
        Arguments.of(List.of(LadderType.TINY), 1, 0L, 0L),
        Arguments.of(List.of(LadderType.TINY), 3, 0L, 0L),
        Arguments.of(List.of(LadderType.SMALL), 1, 80L, 120L),
        Arguments.of(List.of(LadderType.SMALL), 3, 240L, 360L),
        Arguments.of(List.of(LadderType.BIG), 1, 2400L, 3600L),
        Arguments.of(List.of(LadderType.BIG), 3, 7200L, 10800L),
        Arguments.of(List.of(LadderType.GIGANTIC), 1, 8000L, 12000L),
        Arguments.of(List.of(LadderType.GIGANTIC), 3, 24000L, 36000L),
        Arguments.of(List.of(LadderType.SMALL, LadderType.FREE_AUTO, LadderType.CHEAP),
            3, 240L, 360L),
        Arguments.of(List.of(LadderType.BIG, LadderType.NO_AUTO, LadderType.BOUNTIFUL),
            3, 7200L, 10800L),
        Arguments.of(List.of(LadderType.ASSHOLE, LadderType.NO_AUTO), 1, 800L, 1200L),
        Arguments.of(List.of(LadderType.END), 1, 800L, 1200L)
    );
  }
}
