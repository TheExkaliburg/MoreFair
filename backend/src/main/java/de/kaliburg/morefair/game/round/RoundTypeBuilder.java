package de.kaliburg.morefair.game.round;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoundTypeBuilder {

  private static final Random random = new Random();
  private final Map<RoundType, Float> roundSpeedTypeWeights = new HashMap<>();
  private final Map<RoundType, Float> roundAutoTypeWeights = new HashMap<>();
  private final Map<RoundType, Float> roundChaosTypeWeights = new HashMap<>();

  @Setter
  @Accessors(chain = true)
  private Set<RoundType> previousRoundType = EnumSet.noneOf(RoundType.class);

  @Setter
  @Accessors(chain = true)
  private Integer roundNumber = 1;

  public RoundTypeBuilder() {
    roundSpeedTypeWeights.put(RoundType.FAST, 20.f);
    roundSpeedTypeWeights.put(RoundType.SLOW, 10.f);
    roundSpeedTypeWeights.put(RoundType.DEFAULT, 70.f);

    roundAutoTypeWeights.put(RoundType.AUTO, 10.f);
    roundAutoTypeWeights.put(RoundType.DEFAULT, 90.f);

    roundChaosTypeWeights.put(RoundType.CHAOS, 10.f);
    roundChaosTypeWeights.put(RoundType.DEFAULT, 90.f);
  }

  // TODO: add reference to last Round into this method
  private void handlePreviousRoundType(RoundType roundType) {
    // Logic goes here
  }

  public Set<RoundType> build() {
    if (roundNumber == 200) {
      return EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING);
    } else if (roundNumber == 300) {
      return EnumSet.of(RoundType.CHAOS);
    } else if (roundNumber == 301) {
      return EnumSet.of(RoundType.DEFAULT);
    } else if (roundNumber % 10 == 0) {
      roundSpeedTypeWeights.put(RoundType.FAST, 1.f);
      roundSpeedTypeWeights.put(RoundType.SLOW, 1.f);
      roundSpeedTypeWeights.put(RoundType.DEFAULT, 1.f);

      roundAutoTypeWeights.put(RoundType.AUTO, 1.f);
      roundAutoTypeWeights.put(RoundType.DEFAULT, 1.f);

      roundChaosTypeWeights.put(RoundType.CHAOS, 1.f);
      roundChaosTypeWeights.put(RoundType.DEFAULT, 1.f);
    }

    Set<RoundType> roundTypes;
    int iterations = 0;
    do {
      roundTypes = EnumSet.noneOf(RoundType.class);
      roundTypes.stream().sorted(new RoundType.Comparator()).forEach(this::handlePreviousRoundType);
      roundTypes.add(getRandomLadderType(roundSpeedTypeWeights, "Speed"));
      roundTypes.add(getRandomLadderType(roundAutoTypeWeights, "Auto"));
      roundTypes.add(getRandomLadderType(roundChaosTypeWeights, "Chaos"));
      if (roundTypes.size() > 1) {
        roundTypes.remove(RoundType.DEFAULT);
      }
      iterations++;
    } while (previousRoundType.equals(roundTypes) && iterations < 100);
    log.debug("Rerolled {} times to not get a duplicate round.", iterations);
    return roundTypes;
  }

  private Map<Float, RoundType> createInverseLookupTable(Map<RoundType, Float> weights) {
    try {
      Map<Float, RoundType> inverseLookupTable = new HashMap<>();
      float currentWeight = 0;
      for (Map.Entry<RoundType, Float> entry : weights.entrySet()) {
        if (entry.getValue() <= 0) {
          continue;
        }
        currentWeight += entry.getValue();
        inverseLookupTable.put(currentWeight, entry.getKey());
      }
      return inverseLookupTable;
    } catch (Exception e) {
      log.error("Error creating inverse lookup table", e);
      Map<Float, RoundType> inverseLookupTable = new HashMap<>();
      inverseLookupTable.put(1.f, RoundType.DEFAULT);
      return inverseLookupTable;
    }
  }

  private RoundType getRandomLadderType(Map<RoundType, Float> weights, String categoryName) {
    try {
      float totalWeight = weights.values().stream().reduce(0.f, Float::sum);
      float randomNumber = random.nextFloat(totalWeight);
      List<Entry<Float, RoundType>> sortedInverseLookupEntries = createInverseLookupTable(
          weights).entrySet().stream().sorted(Entry.comparingByKey()).toList();

      log.debug("Random {} percentage for R{}: {}/{}", categoryName, roundNumber, randomNumber,
          totalWeight);
      for (Map.Entry<Float, RoundType> entry : sortedInverseLookupEntries) {
        log.debug("Checking {} percentage: {}/{}", entry.getValue(), entry.getKey(), totalWeight);
        if (randomNumber < entry.getKey()) {
          return entry.getValue();
        }
      }
    } catch (Exception e) {
      log.error("Error getting random ladder type", e);
    }
    return RoundType.DEFAULT;
  }
}
