package de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder;

import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.EnumMap;
import java.util.Map;

public class CostTypeStep extends AbstractWeightedLadderTypeStep {

  @Override
  public Map<LadderType, Double> getWeights() {
    Map<LadderType, Double> weights = new EnumMap<>(LadderType.class);
    weights.put(LadderType.CHEAP, 10.d);
    weights.put(LadderType.EXPENSIVE, 10.d);
    weights.put(LadderType.DEFAULT, 100.d);
    return weights;
  }

  @Override
  protected void handleRoundTypes(RoundType roundType, Map<LadderType, Double> weights) {
    super.handleRoundTypes(roundType, weights);
    switch (roundType) {
      case FAST -> {
        weights.computeIfPresent(LadderType.CHEAP, (k, v) -> v * 2);
        weights.computeIfPresent(LadderType.EXPENSIVE, (k, v) -> v / 2);
      }
      case CHAOS -> {
        weights.put(LadderType.CHEAP, 1.d);
        weights.put(LadderType.EXPENSIVE, 1.d);
        weights.put(LadderType.DEFAULT, 1.d);
      }
      case SLOW -> {
        weights.computeIfPresent(LadderType.CHEAP, (k, v) -> v / 2);
        weights.computeIfPresent(LadderType.EXPENSIVE, (k, v) -> v * 2);
      }
      default -> {
        // do nothing
      }
    }
  }
}
