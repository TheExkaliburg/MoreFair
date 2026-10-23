package de.kaliburg.morefair.game.ladder.model.generation.steps.types;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.EnumMap;
import java.util.Map;

public class GrapesNegativeTypeStep extends AbstractWeightedLadderTypeStep {

  @Override
  public Map<LadderType, Double> getWeights() {
    Map<LadderType, Double> weights = new EnumMap<>(LadderType.class);
    weights.put(LadderType.DEFAULT, 50.d);
    return weights;
  }

  @Override
  protected void handleRoundTypes(RoundType roundType, Map<LadderType, Double> weights) {
    super.handleRoundTypes(roundType, weights);
    switch (roundType) {
      case CHAOS -> {
        weights.putIfAbsent(LadderType.LAVA, 25.d);
        weights.putIfAbsent(LadderType.TAXES, 25.d);
        weights.putIfAbsent(LadderType.VIRUS, 25.d);
        weights.put(LadderType.DEFAULT, 25.d);
      }
      case RAILROAD -> {
        weights.put(LadderType.LAVA, 25.d);
        weights.put(LadderType.TAXES, 25.d);
      }
      case FARMER -> {
        weights.put(LadderType.TAXES, 25.d);
        weights.put(LadderType.VIRUS, 25.d);
      }
      case RACE -> {
        weights.put(LadderType.VIRUS, 25.d);
        weights.put(LadderType.LAVA, 25.d);
      }
      default -> {
        // do nothing
      }
    }
  }

  @Override
  protected void handleExistingLadderType(LadderType ladderType, Map<LadderType, Double> weights) {
    super.handleExistingLadderType(ladderType, weights);
    switch (ladderType) {
      case BOUNTIFUL -> {
        weights.computeIfPresent(LadderType.DEFAULT, (k, v) -> v + weights.getOrDefault(LadderType.LAVA, 0.d));
        weights.put(LadderType.LAVA, 0.d);
      }
      case CONSOLATION -> {
        weights.computeIfPresent(LadderType.DEFAULT, (k, v) -> v + weights.getOrDefault(LadderType.VIRUS, 0.d));
        weights.put(LadderType.VIRUS, 0.d);
      }
      case GENEROUS -> {
        weights.computeIfPresent(LadderType.DEFAULT, (k, v) -> v + weights.getOrDefault(LadderType.TAXES, 0.d));
        weights.put(LadderType.TAXES, 0.d);
      }
      default -> {
        // do nothing
      }
    }
  }
}
