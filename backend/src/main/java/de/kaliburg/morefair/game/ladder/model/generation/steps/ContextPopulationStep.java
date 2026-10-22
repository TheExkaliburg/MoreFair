package de.kaliburg.morefair.game.ladder.model.generation.steps;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import java.util.List;
import java.util.Optional;

public class ContextPopulationStep implements LadderGenerationStep {

  private final Integer assholeLadderNumberOverride;

  public ContextPopulationStep() {
    this(null);
  }

  public ContextPopulationStep(Integer assholeLadderNumberOverride) {
    this.assholeLadderNumberOverride = assholeLadderNumberOverride;
  }

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders, LadderGenerationContext context) {
    context.setAssholeLadderNumber(Optional.ofNullable(assholeLadderNumberOverride)
        .orElse(context.getRoundEntity().getAssholeLadderNumber()));

    return ladders;
  }
}
