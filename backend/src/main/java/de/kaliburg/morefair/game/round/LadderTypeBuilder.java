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
public class LadderTypeBuilder {

  private static final Random random = new Random();
  private final Map<LadderType, Float> ladderSizeTypeWeights = new HashMap<>();
  private final Map<LadderType, Float> ladderAutoTypeWeights = new HashMap<>();
  private final Map<LadderType, Float> ladderCostTypeWeights = new HashMap<>();
  private final Map<LadderType, Float> ladderGrapesTypeWeights = new HashMap<>();
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
    ladderSizeTypeWeights.put(LadderType.TINY, 1.f);
    ladderSizeTypeWeights.put(LadderType.SMALL, 20.f);
    ladderSizeTypeWeights.put(LadderType.BIG, 20.f);
    ladderSizeTypeWeights.put(LadderType.GIGANTIC, 1.f);
    ladderSizeTypeWeights.put(LadderType.DEFAULT, 50.f);

    ladderAutoTypeWeights.put(LadderType.FREE_AUTO, 5.f);
    ladderAutoTypeWeights.put(LadderType.NO_AUTO, 2.f);
    ladderAutoTypeWeights.put(LadderType.DEFAULT, 100.f);

    ladderCostTypeWeights.put(LadderType.CHEAP, 10.f);
    ladderCostTypeWeights.put(LadderType.EXPENSIVE, 10.f);
    ladderCostTypeWeights.put(LadderType.DEFAULT, 100.f);

    ladderGrapesTypeWeights.put(LadderType.BOUNTIFUL, 0.f);
    ladderGrapesTypeWeights.put(LadderType.DROUGHT, 0.f);
    ladderGrapesTypeWeights.put(LadderType.STINGY, 0.f);
    ladderGrapesTypeWeights.put(LadderType.CONSOLATION, 0.f);
    ladderGrapesTypeWeights.put(LadderType.NO_HANDOUTS, 0.f);
    ladderGrapesTypeWeights.put(LadderType.GENEROUS, 0.f);
    ladderGrapesTypeWeights.put(LadderType.DEFAULT, 100.f);
  }

  public static LadderTypeBuilder builder() {
    return new LadderTypeBuilder();
  }

  private void handlePreviousLadderType(LadderType ladderType) {
    // CHAOS disables back to back protection but promotes a different ladder Type each time
    if (roundTypes.contains(RoundType.CHAOS)) {
      if (ladderSizeTypeWeights.containsKey(ladderType)) {
        ladderSizeTypeWeights.put(ladderType, ladderSizeTypeWeights.get(ladderType) / 2);
      }
      if (ladderAutoTypeWeights.containsKey(ladderType)) {
        ladderAutoTypeWeights.put(ladderType, ladderAutoTypeWeights.get(ladderType) / 2);
      }
      if (ladderCostTypeWeights.containsKey(ladderType)) {
        ladderCostTypeWeights.put(ladderType, ladderCostTypeWeights.get(ladderType) / 2);
      }
      if(ladderGrapesTypeWeights.containsKey(ladderType)) {
        ladderGrapesTypeWeights.put(ladderType,ladderGrapesTypeWeights.get(ladderType) / 2);
      }

      return;
    }

    if (roundTypes.contains(RoundType.SLOW)) {
      return;
    }

    switch (ladderType) {
      case BIG, GIGANTIC -> {
        ladderSizeTypeWeights.put(LadderType.BIG, 0.f);
      }
      case NO_AUTO -> {
        ladderAutoTypeWeights.put(LadderType.NO_AUTO,
            ladderAutoTypeWeights.get(LadderType.NO_AUTO) / 2);
      }
      default -> {
        // do nothing
      }
    }
  }

  private void handleRoundTypes(RoundType roundType) {
    switch (roundType) {
      case FAST -> {
        ladderSizeTypeWeights.put(LadderType.TINY, ladderSizeTypeWeights.get(LadderType.TINY) * 2);
        ladderSizeTypeWeights.put(LadderType.BIG, 0.f);
        ladderSizeTypeWeights.put(LadderType.GIGANTIC, 0.f);
        ladderSizeTypeWeights.put(LadderType.DEFAULT, 0.f);
        ladderCostTypeWeights.put(LadderType.CHEAP,
            ladderCostTypeWeights.get(LadderType.CHEAP) * 2);
        ladderCostTypeWeights.put(LadderType.EXPENSIVE,
            ladderCostTypeWeights.get(LadderType.EXPENSIVE) / 2);
      }
      case AUTO -> {
        ladderAutoTypeWeights.put(LadderType.FREE_AUTO,
            Math.max(1.0f, ladderAutoTypeWeights.get(LadderType.FREE_AUTO) * 10));
        ladderAutoTypeWeights.put(LadderType.DEFAULT, 0.f);
      }
      case CHAOS -> {
        ladderSizeTypeWeights.put(LadderType.TINY, 1.f);
        ladderSizeTypeWeights.put(LadderType.SMALL, 1.f);
        ladderSizeTypeWeights.put(LadderType.BIG, 1.f);
        ladderSizeTypeWeights.put(LadderType.GIGANTIC, 1.f);
        ladderSizeTypeWeights.put(LadderType.DEFAULT, 1.f);
        ladderCostTypeWeights.put(LadderType.CHEAP, 1.f);
        ladderCostTypeWeights.put(LadderType.EXPENSIVE, 1.f);
        ladderCostTypeWeights.put(LadderType.DEFAULT, 1.f);
        ladderGrapesTypeWeights.put(LadderType.BOUNTIFUL, 1.f);
        ladderGrapesTypeWeights.put(LadderType.DROUGHT, 1.f);
        ladderGrapesTypeWeights.put(LadderType.STINGY, 1.f);
        ladderGrapesTypeWeights.put(LadderType.CONSOLATION, 1.f);
        ladderGrapesTypeWeights.put(LadderType.NO_HANDOUTS, 1.f);
        ladderGrapesTypeWeights.put(LadderType.GENEROUS, 1.f);
        ladderGrapesTypeWeights.put(LadderType.DEFAULT, 1.f);
      }
      case SLOW -> {
        ladderSizeTypeWeights.put(LadderType.TINY, 0.f);
        ladderSizeTypeWeights.put(LadderType.SMALL, 0.f);
        ladderSizeTypeWeights.put(LadderType.DEFAULT,
            ladderSizeTypeWeights.get(LadderType.DEFAULT) / 5);
        ladderSizeTypeWeights.put(LadderType.GIGANTIC,
            ladderSizeTypeWeights.get(LadderType.GIGANTIC) * 2);
        ladderAutoTypeWeights.put(LadderType.NO_AUTO, 0.f);
        ladderAutoTypeWeights.put(LadderType.FREE_AUTO,
            ladderAutoTypeWeights.get(LadderType.FREE_AUTO) * 2);
        ladderCostTypeWeights.put(LadderType.CHEAP,
            ladderCostTypeWeights.get(LadderType.CHEAP) / 2);
        ladderCostTypeWeights.put(LadderType.EXPENSIVE,
            ladderCostTypeWeights.get(LadderType.EXPENSIVE) * 2);
      }
      //The main ladder type becomes the default ladder type for ladderGrapesType rounds.
      case RAILROAD -> {
        ladderGrapesTypeWeights.put(LadderType.CONSOLATION, 50.f);
        ladderGrapesTypeWeights.put(LadderType.DROUGHT, 20.f);
        ladderGrapesTypeWeights.put(LadderType.STINGY, 20.f);
        ladderGrapesTypeWeights.put(LadderType.DEFAULT, 0.f);
      }
      case FARMER -> {
        ladderGrapesTypeWeights.put(LadderType.BOUNTIFUL, 50.f);
        ladderGrapesTypeWeights.put(LadderType.STINGY, 20.f);
        ladderGrapesTypeWeights.put(LadderType.NO_HANDOUTS, 20.f);
        ladderGrapesTypeWeights.put(LadderType.DEFAULT, 0.f);
      }
      case RACE -> {
        ladderGrapesTypeWeights.put(LadderType.GENEROUS, 50.f);
        ladderGrapesTypeWeights.put(LadderType.DROUGHT, 20.f);
        ladderGrapesTypeWeights.put(LadderType.NO_HANDOUTS, 20.f);
        ladderGrapesTypeWeights.put(LadderType.DEFAULT, 0.f);
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
    Set<LadderType> ladderTypes = EnumSet.noneOf(LadderType.class);

    if (roundTypes.contains(RoundType.SPECIAL_100)) {
      return specialRoundBuilder();
    }

    if (ladderNumber == 1) {
      return EnumSet.of(LadderType.DEFAULT);
    }

    if (ladderNumber > assholeLadderNumber) {
      return EnumSet.of(LadderType.END);
    }

    if (ladderNumber > 25) {
      ladderSizeTypeWeights.put(LadderType.GIGANTIC, 0.f);
    }

    this.roundTypes.stream().sorted(new RoundType.Comparator()).forEach(this::handleRoundTypes);
    this.previousLadderType.stream().sorted(new LadderTypeComparator())
        .forEach(this::handlePreviousLadderType);

    if (ladderNumber.equals(assholeLadderNumber)) {
      ladderAutoTypeWeights.put(LadderType.NO_AUTO,
          Math.max(1.f, ladderAutoTypeWeights.get(LadderType.NO_AUTO)));
      ladderAutoTypeWeights.put(LadderType.FREE_AUTO, 0.f);
      ladderAutoTypeWeights.put(LadderType.DEFAULT, 0.f);
      ladderTypes.add(LadderType.ASSHOLE);
    }

    ladderTypes.add(getRandomLadderType(ladderSizeTypeWeights, "Size"));
    ladderTypes.add(getRandomLadderType(ladderAutoTypeWeights, "Auto"));
    ladderTypes.add(getRandomLadderType(ladderCostTypeWeights, "Cost"));
    ladderTypes.add(getRandomLadderType(ladderGrapesTypeWeights, "Grapes"));
    if (ladderTypes.size() > 1) {
      ladderTypes.remove(LadderType.DEFAULT);
    }
    return ladderTypes;
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
      float totalWeight = weights.values().stream().reduce(0.f, Float::sum);
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
