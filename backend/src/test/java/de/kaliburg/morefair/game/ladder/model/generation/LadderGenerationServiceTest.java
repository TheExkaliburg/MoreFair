package de.kaliburg.morefair.game.ladder.model.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.utils.FairTest;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

@FairTest
class LadderGenerationServiceTest {

  @Autowired
  FairConfig fairConfig;
  @Autowired
  private LadderGenerationService ladderGenerationService;

  @ParameterizedTest
  @MethodSource("normalRoundTypes")
  void generateLadders_allRoundTypes_defaultOnlyAlone(Set<RoundType> roundTypes) {
    RoundEntity round = generateRound(roundTypes);
    round.setAssholeLadderNumber(100);

    List<LadderEntity> ladders = ladderGenerationService.generateLaddersForRound(round);

    assertThat(ladders).allSatisfy(ladder -> {
      if (ladder.getTypes().size() > 1) {
        assertThat(ladder.getTypes())
            .as("types of ladder %s", ladder.getNumber())
            .doesNotContain(LadderType.DEFAULT);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("fastRoundTypes")
  void generateLadders_fastRounds_noBigOrGigantic(Set<RoundType> roundTypes) {
    RoundEntity round = generateRound(roundTypes);
    round.setAssholeLadderNumber(100);

    List<LadderEntity> ladders = ladderGenerationService.generateLaddersForRound(round);

    assertThat(ladders).allSatisfy(ladder ->
        assertThat(ladder.getTypes())
            .as("types of ladder %s", ladder.getNumber())
            .doesNotContain(LadderType.BIG, LadderType.GIGANTIC));
  }

  @Test
  void generateLadder_defaultRound_correctLadderList() {
    for (int i = 0; i < 100; i++) {
      RoundEntity round = generateRound(EnumSet.of(RoundType.DEFAULT));

      List<LadderEntity> list = ladderGenerationService.generateLaddersForRound(round);

      assertThat(list)
          .hasSize(round.getAssholeLadderNumber() + 1);
      assertThat(list).map(LadderEntity::getTypes)
          .doesNotContain(Set.of(LadderType.BOUNTIFUL, LadderType.LAVA, LadderType.CONSOLATION,
              LadderType.VIRUS, LadderType.GENEROUS, LadderType.TAXES));

      assertThat(list.getFirst().getTypes())
          .doesNotContain(LadderType.LAVA, LadderType.TINY, LadderType.FREE_AUTO,
              LadderType.NO_AUTO);
      assertThat(list.get(list.size() - 2).getTypes())
          .contains(LadderType.ASSHOLE, LadderType.NO_AUTO)
          .doesNotContain(LadderType.DEFAULT, LadderType.FREE_AUTO);
      assertThat(list.getLast().getTypes())
          .containsExactly(LadderType.END)
          .doesNotContain(LadderType.DEFAULT);

      // Check Scaling
      assertThat(list.getFirst().getScaling()).isEqualTo(1);
      assertThat(list.get(list.size() - 2).getScaling()).isEqualTo(round.getAssholeLadderNumber());
    }
  }

  @Test
  void generateLadder_special100Round_correctLadderList() {
    RoundEntity round = generateRound(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));

    List<LadderEntity> list = ladderGenerationService.generateLaddersForRound(round);

    assertThat(list)
        .hasSize(101);
    assertThat(list.get(list.size() - 2).getTypes())
        .contains(LadderType.NO_AUTO)
        .doesNotContain(LadderType.ASSHOLE, LadderType.DEFAULT);
    assertThat(list.getLast().getTypes())
        .contains(LadderType.END)
        .doesNotContain(LadderType.DEFAULT);

    // Check Scaling
    assertThat(list.getFirst().getScaling()).isEqualTo(100);
    assertThat(list.get(list.size() - 2).getScaling()).isEqualTo(1);
  }

  private static Stream<Set<RoundType>> normalRoundTypes() {
    return Stream.of(
        EnumSet.of(RoundType.DEFAULT),
        EnumSet.of(RoundType.FAST),
        EnumSet.of(RoundType.SLOW),
        EnumSet.of(RoundType.AUTO),
        EnumSet.of(RoundType.CHAOS),
        EnumSet.of(RoundType.FAST, RoundType.AUTO),
        EnumSet.of(RoundType.FAST, RoundType.CHAOS),
        EnumSet.of(RoundType.SLOW, RoundType.AUTO),
        EnumSet.of(RoundType.SLOW, RoundType.CHAOS),
        EnumSet.of(RoundType.AUTO, RoundType.CHAOS),
        EnumSet.of(RoundType.SLOW, RoundType.AUTO, RoundType.CHAOS),
        EnumSet.of(RoundType.FAST, RoundType.AUTO, RoundType.CHAOS),
        EnumSet.of(RoundType.RACE),
        EnumSet.of(RoundType.FARMER),
        EnumSet.of(RoundType.RAILROAD),
        EnumSet.of(RoundType.RACE, RoundType.AUTO),
        EnumSet.of(RoundType.FARMER, RoundType.AUTO),
        EnumSet.of(RoundType.RAILROAD, RoundType.AUTO),
        EnumSet.of(RoundType.RACE, RoundType.CHAOS),
        EnumSet.of(RoundType.FARMER, RoundType.CHAOS),
        EnumSet.of(RoundType.RAILROAD, RoundType.CHAOS),
        EnumSet.of(RoundType.RACE, RoundType.AUTO, RoundType.CHAOS),
        EnumSet.of(RoundType.FARMER, RoundType.AUTO, RoundType.CHAOS),
        EnumSet.of(RoundType.RAILROAD, RoundType.AUTO, RoundType.CHAOS)
    );
  }

  private static Stream<Set<RoundType>> fastRoundTypes() {
    return normalRoundTypes().filter(types -> types.contains(RoundType.FAST));
  }

  private RoundEntity generateRound(Set<RoundType> types) {
    return RoundEntity.builder()
        .id(1L)
        .uuid(UUID.randomUUID())
        .assholeLadderNumber(15)
        .basePointsRequirement(fairConfig.getBasePointsToPromote())
        .number(1)
        .createdOn(OffsetDateTime.now())
        .types(types)
        .percentageOfAdditionalAssholes(0.5f)
        .seasonId(1L)
        .build();
  }
}