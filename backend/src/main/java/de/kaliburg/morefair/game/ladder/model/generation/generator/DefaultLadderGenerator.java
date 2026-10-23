package de.kaliburg.morefair.game.ladder.model.generation.generator;

import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerator;
import de.kaliburg.morefair.game.ladder.model.generation.steps.*;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder.AutoTypeStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder.CostTypeStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder.DefaultTypeStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder.GrapesNegativeTypeStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder.GrapesPositiveTypeStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder.SizeTypeStep;
import de.kaliburg.morefair.game.ladder.model.generation.steps.types.round.ReverseScalingStep;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultLadderGenerator implements LadderGenerator {

  @Override
  public RoundType getSpecialRoundType() {
    return RoundType.DEFAULT;
  }

  @Override
  public List<LadderGenerationStep> getGenerationSteps() {
    return List.of(
        new ContextPopulationStep(),
        new CreateLaddersStep(),
        new ReverseScalingStep(),
        new SizeTypeStep(),
        new AutoTypeStep(),
        new CostTypeStep(),
        new GrapesPositiveTypeStep(),
        new GrapesNegativeTypeStep(),
        new ProtectLadder1Step(),
        new SetupEndingStep(),
        new DefaultTypeStep(),
        // Population of stats
        new PopulatePointRequirementsStep()
    );
  }


}
