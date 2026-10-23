package de.kaliburg.morefair.game.ladder.model.generation.steps.types.round;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AprilFoolsTypesStep implements LadderGenerationStep {

  private static final Random random = new Random();

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders, LadderGenerationContext context) {
    for (LadderEntity ladder : ladders) {
      ladder.setTypes(getTypes(ladder.getNumber(), context.getAssholeLadderNumber()));
    }
    return ladders;
  }

  private Set<LadderType> getTypes(int number, int assholeLadderNumber) {
    if (number == 1) {
      return goodTypes();
    }
    if (number == assholeLadderNumber) {
      return EnumSet.of(LadderType.ASSHOLE, LadderType.GIGANTIC, LadderType.CHEAP,
          LadderType.NO_AUTO);
    }
    if (number == assholeLadderNumber + 1) {
      return EnumSet.of(LadderType.END);
    }
    if (random.nextInt(100) < 20) {
      return goodTypes();
    }
    return EnumSet.of(LadderType.TINY, LadderType.EXPENSIVE, LadderType.LAVA, LadderType.VIRUS,
        LadderType.TAXES, LadderType.NO_AUTO);
  }

  private Set<LadderType> goodTypes() {
    return EnumSet.of(LadderType.SMALL, LadderType.CHEAP, LadderType.BOUNTIFUL,
        LadderType.GENEROUS, LadderType.CONSOLATION, LadderType.FREE_AUTO);
  }
}
