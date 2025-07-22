package de.kaliburg.morefair.game.ladder.model.generation.generator;

import de.kaliburg.morefair.game.ladder.model.generation.steps.LadderGenerationStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.ReverseScalingStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.SetupEndingStep;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultLadderGenerator implements LadderGenerator {

  @Override
  public RoundType getSpecialRoundType() {
    return RoundType.DEFAULT;
  }

  @Override
  public List<LadderGenerationStep> getGenerationSteps() {
    return List.of(
        new SetupEndingStep(),
        new ReverseScalingStep()
    );
  }


}
