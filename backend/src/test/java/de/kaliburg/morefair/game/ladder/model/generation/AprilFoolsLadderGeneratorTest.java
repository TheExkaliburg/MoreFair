package de.kaliburg.morefair.game.ladder.model.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.generator.AprilFoolsLadderGenerator;
import de.kaliburg.morefair.game.ladder.model.generation.generator.DefaultLadderGenerator;
import de.kaliburg.morefair.game.ladder.model.generation.generator.Special100LadderGenerator;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AprilFoolsLadderGeneratorTest {

  private static final Set<LadderType> GOOD_TYPES = EnumSet.of(
      LadderType.SMALL, LadderType.CHEAP, LadderType.BOUNTIFUL, LadderType.GENEROUS,
      LadderType.CONSOLATION, LadderType.FREE_AUTO);
  private static final Set<LadderType> BAD_TYPES = EnumSet.of(
      LadderType.TINY, LadderType.EXPENSIVE, LadderType.LAVA, LadderType.VIRUS,
      LadderType.TAXES, LadderType.NO_AUTO);

  @ParameterizedTest
  @CsvSource({
      "10, true", "55, true", "100, true",
      "10, false", "55, false", "100, false"
  })
  void generateLadders_preservesAprilFoolsRules(int assholeLadderNumber, boolean reverse) {
    Set<RoundType> types = EnumSet.of(RoundType.APRIL_FOOLS);
    if (reverse) {
      types.add(RoundType.REVERSE_SCALING);
    }
    RoundEntity round = RoundEntity.builder()
        .id(1L)
        .seasonId(1L)
        .number(101)
        .assholeLadderNumber(assholeLadderNumber)
        .basePointsRequirement(BigInteger.valueOf(1000))
        .percentageOfAdditionalAssholes(0.5f)
        .types(types)
        .build();
    LadderGenerationService service = new LadderGenerationService(List.of(
        new DefaultLadderGenerator(), new Special100LadderGenerator(),
        new AprilFoolsLadderGenerator()));

    List<LadderEntity> ladders = service.generateLaddersForRound(round);

    assertThat(ladders).hasSize(assholeLadderNumber + 1);
    assertThat(round.getAssholeLadderNumber()).isEqualTo(assholeLadderNumber);
    assertThat(ladders.getFirst().getTypes()).isEqualTo(GOOD_TYPES);
    assertThat(ladders.get(assholeLadderNumber - 1).getTypes())
        .containsExactlyInAnyOrder(LadderType.ASSHOLE, LadderType.GIGANTIC, LadderType.CHEAP,
            LadderType.NO_AUTO);
    assertThat(ladders.get(assholeLadderNumber).getTypes()).containsExactly(LadderType.END);
    for (int number = 2; number < assholeLadderNumber; number++) {
      assertThat(ladders.get(number - 1).getTypes())
          .as("types of ladder %s", number)
          .isIn(GOOD_TYPES, BAD_TYPES);
    }
    for (int number = 1; number <= ladders.size(); number++) {
      LadderEntity ladder = ladders.get(number - 1);
      int expectedScaling = reverse ? Math.max(101 - number, 1) : number;
      assertThat(ladder.getNumber()).isEqualTo(number);
      assertThat(ladder.getRoundId()).isEqualTo(round.getId());
      assertThat(ladder.getScaling())
          .as("scaling of ladder %s", number)
          .isEqualTo(expectedScaling);
      assertPoints(ladder, expectedScaling);
    }
  }

  private void assertPoints(LadderEntity ladder, int scaling) {
    if (ladder.getTypes().contains(LadderType.TINY)) {
      assertThat(ladder.getBasePointsToPromote()).isZero();
      return;
    }
    long base = 1000L * scaling;
    if (ladder.getTypes().contains(LadderType.SMALL)) {
      base /= 10;
    } else if (ladder.getTypes().contains(LadderType.GIGANTIC)) {
      base *= 10;
    }
    assertThat(ladder.getBasePointsToPromote())
        .as("point requirement of ladder %s", ladder.getNumber())
        .isBetween(BigInteger.valueOf(base * 8 / 10), BigInteger.valueOf(base * 12 / 10));
  }
}
