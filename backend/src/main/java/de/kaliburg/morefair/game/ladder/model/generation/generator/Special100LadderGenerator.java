package de.kaliburg.morefair.game.ladder.model.generation.generator;

import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.steps.LadderGenerationStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.ReverseScalingStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.Special100Step;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Special100LadderGenerator implements LadderGenerator {

  @Override
  public RoundType getSpecialRoundType() {
    return RoundType.SPECIAL_100;
  }

  @Override
  public List<LadderGenerationStep> getGenerationSteps() {
    return List.of(
        new Special100Step(),
        new ReverseScalingStep()
    );
  }

  @Override
  public LadderGenerationContext configureContext(LadderGenerationContext context) {
    context = LadderGenerator.super.configureContext(context);
    context.setAssholeLadderNumber(100);
    return context;
  }
}
