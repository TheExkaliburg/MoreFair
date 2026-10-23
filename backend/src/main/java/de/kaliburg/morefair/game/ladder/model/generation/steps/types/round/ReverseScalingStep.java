package de.kaliburg.morefair.game.ladder.model.generation.steps.types.round;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.List;
import java.util.Optional;

public class ReverseScalingStep implements LadderGenerationStep {

  private final Integer scalingBaseOverride;

  public ReverseScalingStep() {
    this(null);
  }

  public ReverseScalingStep(Integer scalingBaseOverride) {
    this.scalingBaseOverride = scalingBaseOverride;
  }

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders, LadderGenerationContext context) {
    if (!context.getRoundEntity().getTypes().contains(RoundType.REVERSE_SCALING)) {
      return ladders;
    }

    int scalingBase = Optional.ofNullable(scalingBaseOverride)
        .orElse(context.getAssholeLadderNumber());
    for (LadderEntity ladder : ladders) {
      ladder.setScaling(Math.max(1, scalingBase - ladder.getScaling() + 1));
    }
    return ladders;
  }
}
