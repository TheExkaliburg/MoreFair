package de.kaliburg.morefair.game.ladder.model.generation.steps;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.List;

public class ReverseScalingStep implements LadderGenerationStep {

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders, LadderGenerationContext context) {
    if (!context.getRoundEntity().getTypes().contains(RoundType.REVERSE_SCALING)) {
      return ladders;
    }

    for (LadderEntity ladder : ladders) {
      ladder.setScaling(context.getAssholeLadderNumber() - ladder.getScaling() + 1);
    }
    return ladders;
  }
}
