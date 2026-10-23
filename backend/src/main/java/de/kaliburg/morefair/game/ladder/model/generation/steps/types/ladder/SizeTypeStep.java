package de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.EnumMap;
import java.util.Map;


/**
 * The SizeTypeStep class extends {@link AbstractWeightedLadderTypeStep} to define specific behavior
 * for size-type ladder steps. It assigns weights to various ladder types and customizes their
 * handling based on the round type.
 *
 * <p>This class is part of the ladder generation process and provides specialized weight
 * adjustment logic to influence the ladder type distribution for different round types.
 */
public class SizeTypeStep extends AbstractWeightedLadderTypeStep {

  @Override
  public Map<LadderType, Double> getWeights() {
    Map<LadderType, Double> weights = new EnumMap<>(LadderType.class);
    weights.put(LadderType.TINY, 1.d);
    weights.put(LadderType.SMALL, 20.d);
    weights.put(LadderType.BIG, 20.d);
    weights.put(LadderType.GIGANTIC, 1.d);
    weights.put(LadderType.DEFAULT, 50.d);
    return weights;
  }


  @Override
  protected void handleRoundTypes(RoundType roundType, Map<LadderType, Double> weights) {
    super.handleRoundTypes(roundType, weights);
    switch (roundType) {
      case FAST -> {
        weights.computeIfPresent(LadderType.TINY, (k, v) -> v * 2);
        weights.put(LadderType.BIG, 0.d);
        weights.put(LadderType.GIGANTIC, 0.d);
        weights.put(LadderType.DEFAULT, 0.d);
      }
      case CHAOS -> {
        weights.put(LadderType.TINY, 1.d);
        weights.put(LadderType.SMALL, 1.d);
        weights.put(LadderType.BIG, 1.d);
        weights.put(LadderType.GIGANTIC, 1.d);
        weights.put(LadderType.DEFAULT, 1.d);
      }
      case SLOW -> {
        weights.put(LadderType.TINY, 0.d);
        weights.put(LadderType.SMALL, 0.d);
        weights.computeIfPresent(LadderType.DEFAULT, (k, v) -> v / 5);
        weights.computeIfPresent(LadderType.GIGANTIC, (k, v) -> v * 2);
      }
      default -> {
        // do nothing
      }
    }
  }

  @Override
  protected void handleExistingLadder(LadderEntity ladder, Map<LadderType, Double> weights) {
    super.handleExistingLadder(ladder, weights);

    if (ladder.getScaling() >= 25) {
      weights.remove(LadderType.GIGANTIC);
    }
  }

  @Override
  protected void handlePreviousLadder(LadderEntity previousLadder,
      Map<LadderType, Double> weights,
      LadderGenerationContext context) {
    super.handlePreviousLadder(previousLadder, weights, context);

    var roundTypes = context.getRoundEntity().getTypes();
    if (previousLadder == null || roundTypes.contains(RoundType.SLOW) || roundTypes.contains(RoundType.CHAOS)) {
      return;
    }

    if (previousLadder.getTypes().contains(LadderType.BIG) || previousLadder.getTypes().contains(LadderType.GIGANTIC)) {
      weights.remove(LadderType.BIG);
    }
  }
}
