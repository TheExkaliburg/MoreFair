package de.kaliburg.morefair.game.ladder.model.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
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

class Special100LadderGeneratorTest {

  @ParameterizedTest
  @CsvSource({"false, 15", "true, 15", "false, 100", "true, 100"})
  void generateLadders_preservesSpecial100Sequence(boolean reverse, int storedLadderCount) {
    Set<RoundType> types = EnumSet.of(RoundType.SPECIAL_100);
    if (reverse) {
      types.add(RoundType.REVERSE_SCALING);
    }
    RoundEntity round = RoundEntity.builder()
        .id(1L)
        .seasonId(1L)
        .number(100)
        .assholeLadderNumber(storedLadderCount)
        .basePointsRequirement(BigInteger.valueOf(1000))
        .percentageOfAdditionalAssholes(0.5f)
        .types(types)
        .build();
    LadderGenerationService service = new LadderGenerationService(
        List.of(new DefaultLadderGenerator(), new Special100LadderGenerator()));

    List<LadderEntity> ladders = service.generateLaddersForRound(round);

    assertThat(ladders).hasSize(101);
    assertThat(ladders).allSatisfy(ladder ->
        assertThat(ladder.getTypes()).doesNotContain(LadderType.ASSHOLE));
    assertThat(ladders.get(49).getTypes()).contains(LadderType.NO_AUTO);
    assertThat(ladders.get(99).getTypes()).contains(LadderType.NO_AUTO);
    assertThat(ladders.get(100).getTypes()).containsExactlyInAnyOrder(
        LadderType.END, LadderType.CONSOLATION, LadderType.BOUNTIFUL,
        LadderType.GENEROUS, LadderType.CHEAP);
  }

  private void assertPoints(LadderEntity ladder, Set<LadderType> types, int scaling) {
    if (types.contains(LadderType.TINY)) {
      assertThat(ladder.getBasePointsToPromote()).isZero();
      return;
    }
    long base = 1000L * scaling;
    if (types.contains(LadderType.SMALL)) {
      base /= 10;
    } else if (types.contains(LadderType.BIG)) {
      base *= 3;
    } else if (types.contains(LadderType.GIGANTIC)) {
      base *= 10;
    }
    assertThat(ladder.getBasePointsToPromote())
        .as("point requirement of ladder %s", ladder.getNumber())
        .isBetween(BigInteger.valueOf(base * 8 / 10),
            BigInteger.valueOf(base * 12 / 10));
  }
}
