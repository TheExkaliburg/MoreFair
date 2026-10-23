package de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import java.util.List;

/**
 * DefaultTypeStep is an implementation of the LadderGenerationStep interface. This class ensures
 * that each LadderEntity in the provided list of ladders has a valid set of types by applying
 * default rules for the presence of {@code LadderType.DEFAULT}. Specifically:
 *
 * <p>If a LadderEntity does not have any types assigned, the {@code LadderType.DEFAULT} will be
 * added.
 *
 * <p>If a LadderEntity has more than one type, the {@code LadderType.DEFAULT} will be removed if
 * present.
 *
 * <p>This step acts as a safeguard to enforce consistency in the type assignment of ladders during
 * the ladder generation process.
 */
public class DefaultTypeStep implements LadderGenerationStep {

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders,
      LadderGenerationContext context) {
    for (LadderEntity ladder : ladders) {
      var types = ladder.getTypes();

      if (types.isEmpty()) {
        types.add(LadderType.DEFAULT);
      }

      if (types.size() > 1) {
        types.remove(LadderType.DEFAULT);
      }
    }
    return ladders;
  }
}
