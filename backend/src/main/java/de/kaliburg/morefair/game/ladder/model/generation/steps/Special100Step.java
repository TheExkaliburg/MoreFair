package de.kaliburg.morefair.game.ladder.model.generation.steps;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import java.util.List;

public class Special100Step implements LadderGenerationStep {

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders,
      final LadderGenerationContext context) {
    // TODO: Implement special Generation

    ladders.get(ladders.size() - 2).getTypes().add(LadderType.NO_AUTO);
    ladders.get(ladders.size() - 1).getTypes().add(LadderType.END);

    return ladders;
  }
}
