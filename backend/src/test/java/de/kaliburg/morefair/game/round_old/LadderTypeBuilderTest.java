package de.kaliburg.morefair.game.round_old;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.game.ladder.LadderTypeBuilder;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.round.model.RoundType;
import de.kaliburg.morefair.utils.EnableLoggingPropertiesBeforeAll;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
@EnableLoggingPropertiesBeforeAll
class LadderTypeBuilderTest {

  static List<Set<RoundType>> roundTypesList = new ArrayList<>();
  static HashMap<Set<RoundType>, List<Set<LadderType>>> ladderTypesListMap = new HashMap<>();
  static HashMap<Set<RoundType>, HashMap<Set<LadderType>, Integer>> countMapMap = new HashMap<>();

  @BeforeAll
  public static void beforeAll() {
    roundTypesList.add(EnumSet.of(RoundType.DEFAULT));
    roundTypesList.add(EnumSet.of(RoundType.FAST));
    roundTypesList.add(EnumSet.of(RoundType.SLOW));
    roundTypesList.add(EnumSet.of(RoundType.AUTO));
    roundTypesList.add(EnumSet.of(RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.FAST, RoundType.AUTO));
    roundTypesList.add(EnumSet.of(RoundType.FAST, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.SLOW, RoundType.AUTO));
    roundTypesList.add(EnumSet.of(RoundType.SLOW, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.AUTO, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.SLOW, RoundType.AUTO, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.FAST, RoundType.AUTO, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.RACE));
    roundTypesList.add(EnumSet.of(RoundType.FARMER));
    roundTypesList.add(EnumSet.of(RoundType.RAILROAD));
    roundTypesList.add(EnumSet.of(RoundType.RACE, RoundType.AUTO));
    roundTypesList.add(EnumSet.of(RoundType.FARMER, RoundType.AUTO));
    roundTypesList.add(EnumSet.of(RoundType.RAILROAD, RoundType.AUTO));
    roundTypesList.add(EnumSet.of(RoundType.RACE, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.FARMER, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.RAILROAD, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.RACE, RoundType.AUTO, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.FARMER, RoundType.AUTO, RoundType.CHAOS));
    roundTypesList.add(EnumSet.of(RoundType.RAILROAD, RoundType.AUTO, RoundType.CHAOS));

    roundTypesList.forEach(roundTypes -> {
      List<Set<LadderType>> ladderTypesList = new ArrayList<>();
      Set<LadderType> lastRoundTypes = EnumSet.noneOf(LadderType.class);
      for (int i = 0; i < 100; i++) {
        LadderTypeBuilder builder = new LadderTypeBuilder();
        builder.setLadderNumber(2);
        builder.setRoundNumber(1);
        builder.setAssholeLadderNumber(25);
        builder.setRoundTypes(roundTypes);
        builder.setPreviousLadderType(lastRoundTypes);
        Set<LadderType> result = builder.build();
        ladderTypesList.add(result);
        lastRoundTypes = result;
      }
      ladderTypesListMap.put(roundTypes, ladderTypesList);

      HashMap<Set<LadderType>, Integer> countMap = new HashMap<>();
      ladderTypesList.forEach(ladderTypes -> {
        if (ladderTypes.size() > 1) {
          assertThat(ladderTypes).doesNotContain(LadderType.DEFAULT);
        }

        countMap.merge(ladderTypes, 1, Integer::sum);
      });
      countMapMap.put(roundTypes, countMap);
    });
  }

  @Test
  void build_allRoundAndLadderTypes_DefaultOnlyAlone() {
    ladderTypesListMap.forEach((key, value) -> value.forEach(ladderTypes -> {
      if (ladderTypes.size() > 1) {
        assertThat(ladderTypes).doesNotContain(LadderType.DEFAULT);
      }
    }));
  }

  @Test
  void build_FastRoundsAndAllLadderTypes_noBigGigantic() {
    ladderTypesListMap.get(EnumSet.of(RoundType.FAST)).forEach(ladderTypes -> {
      if (ladderTypes.size() > 1) {
        assertThat(ladderTypes).doesNotContain(LadderType.DEFAULT);
      }
    });
  }

  @Test
  void build_FirstLadderOnFastRound_isDefault() {
    Set<RoundType> roundTypes = EnumSet.of(RoundType.FAST);
    Set<LadderType> build = LadderTypeBuilder.builder().setRoundTypes(roundTypes).setRoundNumber(1)
        .setLadderNumber(1).setAssholeLadderNumber(25).build();

    assertThat(build).containsExactly(LadderType.DEFAULT);
  }

  @Test
  void build_NoDefaultOnGrapeRounds() {
    roundTypesList.forEach(roundTypes -> {
      if (roundTypes.contains(RoundType.FARMER) || roundTypes.contains(RoundType.RACE)
          || roundTypes.contains(RoundType.RAILROAD)) {
        for (int i = 2; i <= 25; i++) {
          Set<LadderType> build = LadderTypeBuilder.builder().setRoundTypes(roundTypes)
              .setRoundNumber(1).setLadderNumber(i).setAssholeLadderNumber(25).build();
          //Useful if you want to see what ladder types are being generated.
//          log.info("Ladder " + roundTypes.stream().map(RoundType::toString).collect(Collectors.joining(",")) + " " + i +
//                  " Types: " + build.stream().map(LadderType::toString).collect(Collectors.joining(",")));
          assertThat(build).doesNotContain(LadderType.DEFAULT);
        }
      }
    });
  }

}