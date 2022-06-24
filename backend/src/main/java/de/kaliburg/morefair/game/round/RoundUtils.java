package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.api.FairController;
import org.springframework.stereotype.Component;

@Component
public class RoundUtils {

  public Integer getAssholeLadderNumber(RoundEntity currentRound) {
    return FairController.BASE_ASSHOLE_LADDER + currentRound.getHighestAssholeCount();
  }
}
