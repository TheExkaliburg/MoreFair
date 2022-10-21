package de.kaliburg.morefair.game.round;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class LadderTypeBuilderTest {

  
  @Test
  void buildLadderTypes_multipleTimesAllCategories_noDoubleCategories() {

    List<Set<RoundType>> roundTypesList = new ArrayList<>();
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

    StringBuilder sb = new StringBuilder();

    roundTypesList.forEach(roundTypes -> {
      List<Set<LadderType>> ladderTypesList = new ArrayList<>();
      Set<LadderType> lastRoundTypes = EnumSet.noneOf(LadderType.class);
      for (int i = 0; i < 10000; i++) {
        LadderTypeBuilder builder = new LadderTypeBuilder();
        builder.setLadderNumber(2);
        builder.setAssholeLadderNumber(25);
        builder.setRoundTypes(roundTypes);
        builder.setPreviousLadderType(lastRoundTypes);
        Set<LadderType> result = builder.build();
        ladderTypesList.add(result);
        lastRoundTypes = result;
      }

      HashMap<Set<LadderType>, Integer> countMap = new HashMap<>();
      ladderTypesList.forEach(ladderTypes -> {
        if (ladderTypes.size() > 1) {
          assertThat(ladderTypes).doesNotContain(LadderType.DEFAULT);
        }

        countMap.merge(ladderTypes, 1, Integer::sum);
      });

      sb.append(roundTypes.toString()).append(countMap).append("\n\n");
    });

    log.info("\n{}", sb);
  }

  @Test
  void build_multipleTimesFastRound_noBigGigantic() {
    List<Set<LadderType>> ladderTypesList = new ArrayList<>();
    Set<LadderType> lastRoundTypes = EnumSet.noneOf(LadderType.class);
    for (int i = 0; i < 10000; i++) {
      LadderTypeBuilder builder = new LadderTypeBuilder();
      builder.setLadderNumber(2);
      builder.setAssholeLadderNumber(25);
      builder.setRoundTypes(EnumSet.of(RoundType.FAST));
      builder.setPreviousLadderType(lastRoundTypes);
      Set<LadderType> result = builder.build();
      ladderTypesList.add(result);
      lastRoundTypes = result;
    }

    HashMap<Set<LadderType>, Integer> countMap = new HashMap<>();
    ladderTypesList.forEach(ladderTypes -> {
      if (ladderTypes.size() > 1) {
        assertThat(ladderTypes).doesNotContain(LadderType.DEFAULT);
      }

      countMap.merge(ladderTypes, 1, Integer::sum);
    });

    log.info("{}", countMap);
  }

}