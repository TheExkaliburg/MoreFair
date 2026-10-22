package de.kaliburg.morefair.game.ladder.model.generation.steps;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import java.util.List;

public class ProtectLadder1Step implements LadderGenerationStep {

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders,
      LadderGenerationContext context) {
    if (ladders == null || ladders.isEmpty()) {
      return ladders;
    }

    LadderEntity firstLadder = ladders.getFirst();

    firstLadder.getTypes().remove(LadderType.FREE_AUTO);
    firstLadder.getTypes().remove(LadderType.NO_AUTO);
    firstLadder.getTypes().remove(LadderType.TINY);
    firstLadder.getTypes().remove(LadderType.LAVA);

    return ladders;
  }
}
