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
  private Integer roundNumber;

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
    if (previousRoundType.contains(roundType)) {
      return;
    }

    // Logic goes here

    previousRoundType.add(roundType);
  }

  public Set<RoundType> build() {
    Set<RoundType> roundTypes = EnumSet.noneOf(RoundType.class);

    roundTypes.add(getRandomLadderType(roundSpeedTypeWeights, "Speed"));
    roundTypes.add(getRandomLadderType(roundAutoTypeWeights, "Auto"));
    roundTypes.add(getRandomLadderType(roundChaosTypeWeights, "Chaos"));
    if (roundTypes.size() > 1) {
      roundTypes.remove(RoundType.DEFAULT);
    }
    return roundTypes;
  }

  private Map<Float, RoundType> createInverseLookupTable(Map<RoundType, Float> weights) {
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
  }

  private RoundType getRandomLadderType(Map<RoundType, Float> weights, String categoryName) {
    float totalWeight = weights.values().stream().reduce(0.f, Float::sum);
    float randomNumber = random.nextFloat(totalWeight);
    List<Entry<Float, RoundType>> sortedInverseLookupEntries = createInverseLookupTable(
        weights).entrySet().stream().sorted(Entry.comparingByKey()).toList();

    log.debug("Random {} percentage for R{}: {}/{}", categoryName, roundNumber, randomNumber,
        totalWeight);
    for (Map.Entry<Float, RoundType> entry : sortedInverseLookupEntries) {
      log.trace("Checking {} percentage: {}/{}", entry.getValue(), entry.getKey(), totalWeight);
      if (randomNumber < entry.getKey()) {
        return entry.getValue();
      }
    }
    return RoundType.DEFAULT;
  }


}
