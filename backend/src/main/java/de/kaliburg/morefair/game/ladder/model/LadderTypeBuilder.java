package de.kaliburg.morefair.game.ladder.model;

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
public class LadderTypeBuilder {

  private static final Random random = new Random();
  private final Map<LadderType, Float> sizeTypeWeights = new EnumMap<>(LadderType.class);
  private final Map<LadderType, Float> autoTypeWeights = new EnumMap<>(LadderType.class);
  private final Map<LadderType, Float> costTypeWeights = new EnumMap<>(LadderType.class);
  private final Map<LadderType, Float> grapesPositiveTypeWeights = new EnumMap<>(LadderType.class);
  private final Map<LadderType, Float> grapesNegativeTypeWeights = new EnumMap<>(LadderType.class);
  @Setter
  @Accessors(chain = true)
  private Set<RoundType> roundTypes = EnumSet.noneOf(RoundType.class);
  @Setter
  @Accessors(chain = true)
  private Set<LadderType> previousLadderType = EnumSet.noneOf(LadderType.class);
  @Setter
  @Accessors(chain = true)
  private Integer ladderNumber;
  @Setter
  @Accessors(chain = true)
  private Integer roundNumber = 1;
  @Setter
  @Accessors(chain = true)
  private Integer assholeLadderNumber;

  public LadderTypeBuilder() {
    sizeTypeWeights.put(LadderType.TINY, 1.f);
    sizeTypeWeights.put(LadderType.SMALL, 20.f);
    sizeTypeWeights.put(LadderType.BIG, 20.f);
    sizeTypeWeights.put(LadderType.GIGANTIC, 1.f);
    sizeTypeWeights.put(LadderType.DEFAULT, 50.f);

    autoTypeWeights.put(LadderType.FREE_AUTO, 5.f);
    autoTypeWeights.put(LadderType.NO_AUTO, 2.f);
    autoTypeWeights.put(LadderType.DEFAULT, 100.f);

    costTypeWeights.put(LadderType.CHEAP, 10.f);
    costTypeWeights.put(LadderType.EXPENSIVE, 10.f);
    costTypeWeights.put(LadderType.DEFAULT, 100.f);

    grapesPositiveTypeWeights.put(LadderType.DEFAULT, 1.f);
    grapesNegativeTypeWeights.put(LadderType.DEFAULT, 1.f);
  }

  public static LadderTypeBuilder builder() {
    return new LadderTypeBuilder();
  }

  private void handlePreviousLadderType(LadderType ladderType) {
    // CHAOS disables back to back protection but promotes a different ladder Type each time
    if (roundTypes.contains(RoundType.CHAOS)) {
      sizeTypeWeights.computeIfPresent(ladderType, (k, v) -> v / 2);
      autoTypeWeights.computeIfPresent(ladderType, (k, v) -> v / 2);
      costTypeWeights.computeIfPresent(ladderType, (k, v) -> v / 2);
      grapesPositiveTypeWeights.computeIfPresent(ladderType, (k, v) -> v / 2);
      grapesNegativeTypeWeights.computeIfPresent(ladderType, (k, v) -> v / 2);
      return;
    }

    if (roundTypes.contains(RoundType.SLOW)) {
      return;
    }

    switch (ladderType) {
      case BIG, GIGANTIC -> sizeTypeWeights.put(LadderType.BIG, 0.f);
      case NO_AUTO -> autoTypeWeights.computeIfPresent(LadderType.NO_AUTO, (k, v) -> v / 2);
      default -> {
        // do nothing
      }
    }
  }

  private void handleRoundTypes(RoundType roundType) {
    switch (roundType) {
      case FAST -> {
        sizeTypeWeights.computeIfPresent(LadderType.TINY, (k, v) -> v * 2);
        sizeTypeWeights.put(LadderType.BIG, 0.f);
        sizeTypeWeights.put(LadderType.GIGANTIC, 0.f);
        sizeTypeWeights.put(LadderType.DEFAULT, 0.f);
        costTypeWeights.computeIfPresent(LadderType.CHEAP, (k, v) -> v * 2);
        costTypeWeights.computeIfPresent(LadderType.EXPENSIVE, (k, v) -> v / 2);
      }
      case AUTO -> {
        autoTypeWeights.computeIfPresent(LadderType.FREE_AUTO, (k, v) -> Math.max(1.0f, v * 10));
        autoTypeWeights.put(LadderType.DEFAULT, 0.f);
      }
      case CHAOS -> {
        sizeTypeWeights.put(LadderType.TINY, 1.f);
        sizeTypeWeights.put(LadderType.SMALL, 1.f);
        sizeTypeWeights.put(LadderType.BIG, 1.f);
        sizeTypeWeights.put(LadderType.GIGANTIC, 1.f);
        sizeTypeWeights.put(LadderType.DEFAULT, 1.f);
        costTypeWeights.put(LadderType.CHEAP, 1.f);
        costTypeWeights.put(LadderType.EXPENSIVE, 1.f);
        costTypeWeights.put(LadderType.DEFAULT, 1.f);
        grapesPositiveTypeWeights.putIfAbsent(LadderType.BOUNTIFUL, 25.f);
        grapesPositiveTypeWeights.putIfAbsent(LadderType.GENEROUS, 25.f);
        grapesPositiveTypeWeights.putIfAbsent(LadderType.CONSOLATION, 25.f);
        grapesPositiveTypeWeights.putIfAbsent(LadderType.DEFAULT, 25.f);
        grapesNegativeTypeWeights.putIfAbsent(LadderType.LAVA, 25.f);
        grapesNegativeTypeWeights.putIfAbsent(LadderType.TAXES, 25.f);
        grapesNegativeTypeWeights.putIfAbsent(LadderType.VIRUS, 25.f);
        grapesNegativeTypeWeights.putIfAbsent(LadderType.DEFAULT, 25.f);
      }
      case SLOW -> {
        sizeTypeWeights.put(LadderType.TINY, 0.f);
        sizeTypeWeights.put(LadderType.SMALL, 0.f);
        sizeTypeWeights.computeIfPresent(LadderType.DEFAULT, (k, v) -> v / 5);
        sizeTypeWeights.computeIfPresent(LadderType.GIGANTIC, (k, v) -> v * 2);
        autoTypeWeights.put(LadderType.NO_AUTO, 0.f);
        autoTypeWeights.computeIfPresent(LadderType.FREE_AUTO, (k, v) -> v * 2);
        costTypeWeights.computeIfPresent(LadderType.CHEAP, (k, v) -> v / 2);
        costTypeWeights.computeIfPresent(LadderType.EXPENSIVE, (k, v) -> v * 2);
      }
      //The main ladder type becomes the default ladder type for ladderGrapesType rounds.
      case RAILROAD -> {
        grapesPositiveTypeWeights.put(LadderType.CONSOLATION, 75.f);
        grapesPositiveTypeWeights.put(LadderType.DEFAULT, 25.f);
        grapesNegativeTypeWeights.put(LadderType.LAVA, 25.f);
        grapesNegativeTypeWeights.put(LadderType.TAXES, 25.f);
        grapesNegativeTypeWeights.put(LadderType.DEFAULT, 50.f);
        autoTypeWeights.computeIfPresent(LadderType.FREE_AUTO, (k, v) -> v * 2);
      }
      case FARMER -> {
        grapesPositiveTypeWeights.put(LadderType.BOUNTIFUL, 75.f);
        grapesPositiveTypeWeights.put(LadderType.DEFAULT, 25.f);
        grapesNegativeTypeWeights.put(LadderType.TAXES, 25.f);
        grapesNegativeTypeWeights.put(LadderType.VIRUS, 25.f);
        grapesNegativeTypeWeights.put(LadderType.DEFAULT, 50.f);
      }
      case RACE -> {
        grapesPositiveTypeWeights.put(LadderType.GENEROUS, 75.f);
        grapesPositiveTypeWeights.put(LadderType.DEFAULT, 25.f);
        grapesNegativeTypeWeights.put(LadderType.LAVA, 25.f);
        grapesNegativeTypeWeights.put(LadderType.VIRUS, 25.f);
        grapesNegativeTypeWeights.put(LadderType.DEFAULT, 50.f);
        autoTypeWeights.computeIfPresent(LadderType.NO_AUTO, (k, v) -> v * 2);
      }
      default -> {
        // do nothing
      }
    }
  }

  /**
   * Uses the current state of the builder to determine the ladder type.
   *
   * @return a random ladder type based on the current round and ladder number
   */
  public Set<LadderType> build() {

    if (roundTypes.contains(RoundType.SPECIAL_100)) {
      return specialRoundBuilder();
    }

    if (ladderNumber > assholeLadderNumber) {
      return EnumSet.of(LadderType.END);
    }

    this.roundTypes.stream().sorted(new RoundType.Comparator()).forEach(this::handleRoundTypes);
    this.previousLadderType.stream().sorted(new LadderType.Comparator())
        .forEach(this::handlePreviousLadderType);

    if (ladderNumber > 25) {
      sizeTypeWeights.put(LadderType.GIGANTIC, 0.f);
    }

    if (ladderNumber == 1) {
      autoTypeWeights.put(LadderType.LAVA, 0.f);
      sizeTypeWeights.put(LadderType.TINY, 0.f);
      autoTypeWeights.put(LadderType.FREE_AUTO, 0.f);
      autoTypeWeights.put(LadderType.NO_AUTO, 0.f);
    }

    Set<LadderType> ladderTypes = EnumSet.noneOf(LadderType.class);

    if (ladderNumber.equals(assholeLadderNumber)) {
      autoTypeWeights.put(LadderType.NO_AUTO, 1.f);
      autoTypeWeights.put(LadderType.FREE_AUTO, 0.f);
      autoTypeWeights.put(LadderType.DEFAULT, 0.f);
      ladderTypes.add(LadderType.ASSHOLE);
    }

    ladderTypes.add(getRandomLadderType(sizeTypeWeights, "Size"));
    ladderTypes.add(getRandomLadderType(autoTypeWeights, "Auto"));
    ladderTypes.add(getRandomLadderType(costTypeWeights, "Cost"));
    ladderTypes.add(getRandomLadderType(grapesPositiveTypeWeights, "GrapesPositive"));
    ladderTypes.add(getRandomLadderType(grapesNegativeTypeWeights, "GrapesNegative"));

    postProcess(ladderTypes);

    return ladderTypes;
  }

  private void postProcess(Set<LadderType> ladderTypes) {
    if (ladderTypes.containsAll(EnumSet.of(LadderType.BOUNTIFUL, LadderType.LAVA))) {
      ladderTypes.remove(LadderType.LAVA);
    }
    if (ladderTypes.containsAll(EnumSet.of(LadderType.CONSOLATION, LadderType.VIRUS))) {
      ladderTypes.remove(LadderType.VIRUS);
    }
    if (ladderTypes.containsAll(EnumSet.of(LadderType.GENEROUS, LadderType.TAXES))) {
      ladderTypes.remove(LadderType.TAXES);
    }

    if (ladderTypes.size() > 1) {
      ladderTypes.remove(LadderType.DEFAULT);
    }
  }

  private Map<Float, LadderType> createInverseLookupTable(Map<LadderType, Float> weights) {
    try {
      Map<Float, LadderType> inverseLookupTable = new HashMap<>();
      float currentWeight = 0;
      for (Map.Entry<LadderType, Float> entry : weights.entrySet()) {
        if (entry.getValue() <= 0) {
          continue;
        }
        currentWeight += entry.getValue();
        inverseLookupTable.put(currentWeight, entry.getKey());
      }
      return inverseLookupTable;
    } catch (Exception e) {
      log.error("Error creating inverse lookup table", e);
      Map<Float, LadderType> inverseLookupTable = new HashMap<>();
      inverseLookupTable.put(1.f, LadderType.DEFAULT);
      return inverseLookupTable;
    }
  }

  private LadderType getRandomLadderType(Map<LadderType, Float> weights, String categoryName) {
    try {
      if (weights.isEmpty()) {
        return LadderType.DEFAULT;
      }
      float totalWeight = weights.values().stream().reduce(0.f, Float::sum);
      if (totalWeight <= 0) {
        return LadderType.DEFAULT;
      }

      float randomNumber = random.nextFloat(totalWeight);
      List<Entry<Float, LadderType>> inverseLookupEntries = createInverseLookupTable(
          weights).entrySet().stream().sorted(Entry.comparingByKey()).toList();

      log.debug("Random {} percentage for L{}: {}/{}", categoryName, ladderNumber, randomNumber,
          totalWeight);
      for (Map.Entry<Float, LadderType> entry : inverseLookupEntries) {
        log.debug("Checking {} percentage: {}/{}", entry.getValue(), entry.getKey(), totalWeight);
        if (randomNumber < entry.getKey()) {
          return entry.getValue();
        }
      }
    } catch (Exception e) {
      log.error("Error getting random ladder type. Category: {}, Weights: {}, RoundTypes: {}",
          categoryName,
          weights,
          roundTypes,
          e);
    }
    return LadderType.DEFAULT;
  }

  private Set<LadderType> specialRoundBuilder() {
    Set<LadderType> result = EnumSet.noneOf(LadderType.class);

    if (ladderNumber > 100) {
      return EnumSet.of(LadderType.END);
    }

    if (ladderNumber == 50 || ladderNumber == 100) {
      result.add(LadderType.NO_AUTO);
    }

    if ((ladderNumber % 10 == 0 && ladderNumber != 90) || ladderNumber == 1) {
      result.add(LadderType.GIGANTIC);
      result.add(LadderType.CHEAP);
    } else {
      if (ladderNumber > 60 && ladderNumber < 70) {
        result.add(LadderType.SMALL);
      } else if (ladderNumber >= 91) {
        int modulo = (ladderNumber - 1) % 5;
        switch (modulo) {
          case 1:
            result.add(LadderType.SMALL);
            break;
          case 2:
            result.add(LadderType.DEFAULT);
            break;
          case 3:
            result.add(LadderType.BIG);
            break;
          case 4:
            result.add(LadderType.GIGANTIC);
            break;
          case 0:
          default:
            result.add(LadderType.TINY);
            break;
        }
      } else {
        result.add(LadderType.TINY);
      }

    }

    return result;
  }
}
