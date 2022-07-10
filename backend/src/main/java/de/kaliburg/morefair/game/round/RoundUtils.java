package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class RoundUtils {

  private final Random random = new Random();
  private final FairConfig config;

  public RoundUtils(FairConfig config) {
    this.config = config;
  }

  public Integer getAssholeLadderNumber(RoundEntity currentRound) {
    return config.getBaseAssholeLadder() + currentRound.getHighestAssholeCount();
  }
}
