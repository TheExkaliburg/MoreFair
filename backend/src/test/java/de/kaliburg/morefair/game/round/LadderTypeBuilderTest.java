package de.kaliburg.morefair.game.round;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class LadderTypeBuilderTest {

  static List<Set<RoundType>> roundTypesList = new ArrayList<>();
  static HashMap<Set<RoundType>, List<Set<LadderType>>> ladderTypesListMap = new HashMap<>();
  static HashMap<Set<RoundType>, HashMap<Set<LadderType>, Integer>> countMapMap = new HashMap<>();

  @BeforeAll
  static void init() {
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

    roundTypesList.forEach(roundTypes -> {
      List<Set<LadderType>> ladderTypesList = new ArrayList<>();
      Set<LadderType> lastRoundTypes = EnumSet.noneOf(LadderType.class);
      for (int i = 0; i < 100; i++) {
        LadderTypeBuilder builder = new LadderTypeBuilder();
        builder.setLadderNumber(2);
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
    log.info("{}", countMapMap);
  }

  @Test
  void build_FastRoundsAndAllLadderTypes_noBigGigantic() {
    ladderTypesListMap.get(EnumSet.of(RoundType.FAST)).forEach(ladderTypes -> {
      if (ladderTypes.size() > 1) {
        assertThat(ladderTypes).doesNotContain(LadderType.DEFAULT);
      }
    });
    log.info("FAST: {}", countMapMap.get(EnumSet.of(RoundType.FAST)));
  }

  @Test
  void test() {

    for (int i = 0; i < 10; i++) {
      Set<LadderType> set = new HashSet<>();
      set.add(LadderType.DEFAULT);
      set.add(LadderType.SMALL);
      set.add(LadderType.TINY);
      set.add(LadderType.NO_AUTO);
      set.add(LadderType.FREE_AUTO);
      set.add(LadderType.BIG);
      System.out.println("");
      set.stream().sorted(new LadderTypeComparator()).forEach(ladderType -> {
        System.out.print(ladderType + " " + ladderType.ordinal() + ", ");
      });
    }
  }

}