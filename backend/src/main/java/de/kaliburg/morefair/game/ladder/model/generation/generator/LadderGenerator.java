package de.kaliburg.morefair.game.ladder.model.generation.generator;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.steps.LadderGenerationStep;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public interface LadderGenerator {

  RoundType getSpecialRoundType();

  List<LadderGenerationStep> getGenerationSteps();

  default List<LadderEntity> generateLadders(LadderGenerationContext context) {
    context = configureContext(context);

    List<LadderEntity> result = initializeLadders(context);

    for (LadderGenerationStep step : getGenerationSteps()) {
      result = step.apply(result, context);
    }

    return cleanup(result);
  }

  default LadderGenerationContext configureContext(LadderGenerationContext context) {
    return context;
  }

  default List<LadderEntity> initializeLadders(LadderGenerationContext context) {
    List<LadderEntity> result = new ArrayList<>();

    for (int i = 1; i <= context.getAssholeLadderNumber() + 1; i++) {
      LadderEntity ladder = LadderEntity.builder()
          .roundId(context.getRoundEntity().getId())
          .types(EnumSet.noneOf(LadderType.class))
          .number(i)
          .scaling(i)
          .basePointsToPromote(BigInteger.ZERO)
          .build();

      result.add(ladder);
    }
    return result;
  }

  default List<LadderEntity> cleanup(List<LadderEntity> ladders) {
    // Removing DEFAULT from ladders with modifiers
    ladders.stream().filter(l -> l.getTypes().size() > 1)
        .forEach(l -> l.getTypes().remove(LadderType.DEFAULT));

    // Filling everything that doesn't have a type with DEFAULT
    ladders.stream().filter(l -> l.getTypes().isEmpty())
        .forEach(l -> l.getTypes().add(LadderType.DEFAULT));

    return ladders;
  }
}
