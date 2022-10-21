package de.kaliburg.morefair.game.round;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
class RoundTypeBuilderTest {

  @Test
  void buildRoundTypes_multipleTimes_noDoubleCategories() {
    List<Set<RoundType>> roundTypesList = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      RoundTypeBuilder builder = new RoundTypeBuilder();
      roundTypesList.add(builder.build());
    }

    HashMap<Set<RoundType>, Integer> countMap = new HashMap<>();

    roundTypesList.forEach(roundTypes -> {
      if (roundTypes.size() > 1) {
        assertThat(roundTypes).doesNotContain(RoundType.DEFAULT);
      }

      countMap.merge(roundTypes, 1, Integer::sum);
    });

    log.info("{}", countMap);
  }
}