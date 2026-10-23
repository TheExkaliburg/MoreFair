package de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * The AutoTypeStep class extends {@link AbstractWeightedLadderTypeStep} to define specific behavior
 * for auto-type ladder steps. It assigns weights to various ladder types and customizes their
 * handling based on the round type.
 *
 * <p>This class is part of the ladder generation process and provides specialized weight
 * adjustment logic to influence the ladder type distribution for different round types.
 */
public class AutoTypeStep extends AbstractWeightedLadderTypeStep {

  @Override
  public Map<LadderType, Double> getWeights() {
    Map<LadderType, Double> weights = new EnumMap<>(LadderType.class);
    weights.put(LadderType.FREE_AUTO, 5.d);
    weights.put(LadderType.NO_AUTO, 2.d);
    weights.put(LadderType.DEFAULT, 100.d);
    return weights;
  }

  @Override
  protected void handleRoundTypes(RoundType roundType, Map<LadderType, Double> weights) {
    super.handleRoundTypes(roundType, weights);
    switch (roundType) {
      case AUTO -> {
        weights.computeIfPresent(LadderType.FREE_AUTO, (k, v) -> Math.max(1.0d, v * 10));
        weights.put(LadderType.DEFAULT, 0.d);
      }
      case SLOW -> {
        weights.put(LadderType.NO_AUTO, 0.d);
        weights.computeIfPresent(LadderType.FREE_AUTO, (k, v) -> v * 2);
      }
      case RAILROAD -> weights.computeIfPresent(LadderType.FREE_AUTO, (k, v) -> v * 2);
      case RACE -> weights.computeIfPresent(LadderType.NO_AUTO, (k, v) -> v * 2);
      default -> {
        // do nothing
      }
    }
  }

  @Override
  protected void handlePreviousLadder(LadderEntity previousLadder,
      Map<LadderType, Double> weights,
      LadderGenerationContext context) {
    super.handlePreviousLadder(previousLadder, weights, context);
    Set<RoundType> roundTypes = context.getRoundEntity().getTypes();
    if (previousLadder == null || roundTypes.contains(RoundType.SLOW) || roundTypes.contains(RoundType.CHAOS)) {
      return;
    }

    if (previousLadder.getTypes().contains(LadderType.NO_AUTO)) {
      weights.remove(LadderType.NO_AUTO);
    }
  }
}
