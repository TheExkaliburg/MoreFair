package de.kaliburg.morefair.game.round.model;

import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.EnumMap;
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
public class RoundTypeSetBuilder {

  private static final Random random = new Random();
  private final Map<RoundType, Float> speedTypeWeights = new EnumMap<>(RoundType.class);
  private final Map<RoundType, Float> lengthTypeWeights = new EnumMap<>(RoundType.class);
  private final Map<RoundType, Float> autoTypeWeights = new EnumMap<>(RoundType.class);
  private final Map<RoundType, Float> chaosTypeWeights = new EnumMap<>(RoundType.class);
  private final Map<RoundType, Float> grapesTypeWeights = new EnumMap<>(RoundType.class);
  private final Map<RoundType, Float> scalingTypeWeights = new EnumMap<>(RoundType.class);

  @Setter
  @Accessors(chain = true)
  private Set<RoundType> previousRoundType = EnumSet.noneOf(RoundType.class);

  @Setter
  @Accessors(chain = true)
  private Integer roundNumber = 1;

  public RoundTypeSetBuilder() {
    speedTypeWeights.put(RoundType.FAST, 20.f);
    speedTypeWeights.put(RoundType.SLOW, 10.f);
    speedTypeWeights.put(RoundType.DEFAULT, 70.f);

    lengthTypeWeights.put(RoundType.SHORT, 20.f);
    lengthTypeWeights.put(RoundType.LONG, 10.f);
    lengthTypeWeights.put(RoundType.DEFAULT, 70.f);

    autoTypeWeights.put(RoundType.AUTO, 10.f);
    autoTypeWeights.put(RoundType.DEFAULT, 90.f);

    chaosTypeWeights.put(RoundType.CHAOS, 10.f);
    chaosTypeWeights.put(RoundType.DEFAULT, 90.f);

    grapesTypeWeights.put(RoundType.FARMER, 10.f);
    grapesTypeWeights.put(RoundType.RAILROAD, 10.f);
    grapesTypeWeights.put(RoundType.RACE, 10.f);
    grapesTypeWeights.put(RoundType.DEFAULT, 60.f);

    scalingTypeWeights.put(RoundType.REVERSE_SCALING, 0.f);
    scalingTypeWeights.put(RoundType.DEFAULT, 1.f);
  }

  private void handlePreviousRoundType(RoundType roundType) {
    // currently not doing anything to the modifiers based on previous Ladder
  }

  public Set<RoundType> build() {
    if (roundNumber % 10 == 0) {
      speedTypeWeights.put(RoundType.FAST, 1.f);
      speedTypeWeights.put(RoundType.SLOW, 1.f);
      speedTypeWeights.put(RoundType.DEFAULT, 1.f);

      lengthTypeWeights.put(RoundType.SHORT, 1.f);
      lengthTypeWeights.put(RoundType.LONG, 1.f);
      lengthTypeWeights.put(RoundType.DEFAULT, 1.f);

      autoTypeWeights.put(RoundType.AUTO, 1.f);
      autoTypeWeights.put(RoundType.DEFAULT, 1.f);

      chaosTypeWeights.put(RoundType.CHAOS, 1.f);
      chaosTypeWeights.put(RoundType.DEFAULT, 1.f);

      grapesTypeWeights.put(RoundType.FARMER, 1.f);
      grapesTypeWeights.put(RoundType.RAILROAD, 1.f);
      grapesTypeWeights.put(RoundType.RACE, 1.f);
      grapesTypeWeights.put(RoundType.DEFAULT, 1.f);

      scalingTypeWeights.put(RoundType.REVERSE_SCALING, 1.f);
      scalingTypeWeights.put(RoundType.DEFAULT, 1.f);
    }

    Set<RoundType> roundTypes;
    int iterations = 0;
    do {
      roundTypes = EnumSet.of(RoundType.DEFAULT);
      roundTypes.stream().sorted(new RoundType.Comparator()).forEach(this::handlePreviousRoundType);
      roundTypes.add(getRandomLadderType(speedTypeWeights, "Speed"));
      roundTypes.add(getRandomLadderType(lengthTypeWeights, "Length"));
      roundTypes.add(getRandomLadderType(autoTypeWeights, "Auto"));
      roundTypes.add(getRandomLadderType(chaosTypeWeights, "Chaos"));
      roundTypes.add(getRandomLadderType(grapesTypeWeights, "Grapes"));
      roundTypes.add(getRandomLadderType(scalingTypeWeights, "Scaling"));

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
      if (weights.isEmpty()) {
        return RoundType.DEFAULT;
      }
      float totalWeight = weights.values().stream().reduce(0.f, Float::sum);
      if (totalWeight <= 0) {
        return RoundType.DEFAULT;
      }
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
