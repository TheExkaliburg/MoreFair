package de.kaliburg.morefair.game.round.services;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import org.springframework.stereotype.Component;

@Component
public class RoundUtils {

  public RoundUtils() {
  }

  public Integer getAssholeLadderNumber(RoundEntity currentRound) {
    return currentRound.getAssholeLadderNumber();
  }

  public Integer getAssholesNeededForReset(RoundEntity currentRound) {
    int max = currentRound.getAssholeLadderNumber();
    int min = currentRound.getBaseAssholeLadder() / 2;

    return min + Math.round((max - min) * currentRound.getPercentageOfAdditionalAssholes());
  }
}
