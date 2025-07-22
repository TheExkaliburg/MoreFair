package de.kaliburg.morefair.game.ladder.model.generation.steps;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import java.util.List;

/**
 * An Interface that is the generation of the
 */
public interface LadderGenerationStep {

  List<LadderEntity> apply(List<LadderEntity> ladders, final LadderGenerationContext context);
}
