package de.kaliburg.morefair.game.ladder.model.generation.steps.types.round;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Special100Step implements LadderGenerationStep {

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders,
      final LadderGenerationContext context) {
    for (LadderEntity ladder : ladders) {
      ladder.setTypes(getTypes(ladder.getNumber()));
    }
    return ladders;
  }

  private Set<LadderType> getTypes(int number) {
    if (number > 100) {
      return EnumSet.of(LadderType.END, LadderType.CONSOLATION, LadderType.BOUNTIFUL,
          LadderType.GENEROUS, LadderType.CHEAP);
    }

    Set<LadderType> types = EnumSet.noneOf(LadderType.class);
    if (number == 50 || number == 100) {
      types.add(LadderType.NO_AUTO);
    }

    if (number == 1 || (number % 10 == 0 && number != 90)) {
      types.addAll(EnumSet.of(LadderType.GIGANTIC, LadderType.CHEAP, LadderType.GENEROUS));
    } else if (number > 60 && number < 70) {
      types.addAll(EnumSet.of(LadderType.SMALL, LadderType.CONSOLATION, LadderType.LAVA,
          LadderType.TAXES));
    } else if (number >= 91) {
      types.addAll(EnumSet.of(LadderType.TAXES, LadderType.VIRUS, LadderType.LAVA));
      types.add(switch ((number - 1) % 5) {
        case 1 -> LadderType.SMALL;
        case 2 -> LadderType.DEFAULT;
        case 3 -> LadderType.BIG;
        case 4 -> LadderType.GIGANTIC;
        default -> LadderType.TINY;
      });
    } else {
      types.addAll(EnumSet.of(LadderType.TINY, LadderType.EXPENSIVE, LadderType.BOUNTIFUL));
    }
    return types;
  }
}
