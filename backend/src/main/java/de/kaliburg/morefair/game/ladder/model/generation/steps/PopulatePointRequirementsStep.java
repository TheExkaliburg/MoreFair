package de.kaliburg.morefair.game.ladder.model.generation.steps;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.UnaryOperator;

public class PopulatePointRequirementsStep implements LadderGenerationStep {

   private static final Random random = new Random();

   private static final Map<LadderType, UnaryOperator<BigDecimal>> TYPE_MODIFIERS = Map.of(
           LadderType.TINY, value -> BigDecimal.ZERO,
           LadderType.SMALL, value -> value.movePointLeft(1), // Divide by 10
           LadderType.BIG, value -> value.multiply(BigDecimal.valueOf(3)),
           LadderType.GIGANTIC, value -> value.multiply(BigDecimal.valueOf(10))
   );

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders, LadderGenerationContext context) {
      BigInteger basePointsRequirement = context.getRoundEntity().getBasePointsRequirement();

      for (LadderEntity ladder : ladders) {
        var points = calculate(basePointsRequirement, ladder.getScaling(), ladder.getTypes());
        ladder.setBasePointsToPromote(points);
      }
    return ladders;
  }

  private BigInteger calculate(BigInteger base, int scaling, Set<LadderType> types) {
      double percentage = random.nextDouble(0.8, Math.nextUp(1.2));
      BigDecimal baseDec = new BigDecimal(base);
      baseDec = baseDec
              .multiply(BigDecimal.valueOf(scaling))
              .multiply(BigDecimal.valueOf(percentage));
      for (LadderType type : types) {
          baseDec = TYPE_MODIFIERS.getOrDefault(type, UnaryOperator.identity()).apply(baseDec);
      }


      return baseDec.toBigInteger();
  }
}
