package de.kaliburg.morefair.game.ladder.model.generation;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import lombok.Data;

@Data
public class LadderGenerationContext {

  private RoundEntity roundEntity;
  private int assholeLadderNumber;

  public LadderGenerationContext(RoundEntity roundEntity) {
    this.roundEntity = roundEntity;
    this.assholeLadderNumber = roundEntity.getAssholeLadderNumber();
  }

}
