package de.kaliburg.morefair.game;

import de.kaliburg.morefair.FairConfig;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class UpgradeUtils {

  private final FairConfig config;

  public UpgradeUtils(FairConfig config) {
    this.config = config;
  }

  /**
   * Calculates the cost of the next bias/multi upgrade.
   *
   * <p><code>cost = (ladder + 1) ^ (currentUpgrade + 1)</code>
   *
   * @param currentUpgrade the current amount of upgrades
   * @param ladderNumber   the ladder where you would buy the upgrade
   * @return the cost of the (currentUpgrade + 1)th bias/multi
   */
  public BigInteger buyUpgradeCost(Integer ladderNumber, Integer currentUpgrade) {
    BigInteger ladder = BigInteger.valueOf(ladderNumber + 1);
    BigInteger result = ladder.pow(currentUpgrade + 1);

    return result;
  }

  public BigInteger throwVinegarCost(Integer ladderNum) {
    return config.getBaseVinegarToThrow().multiply(BigInteger.valueOf(ladderNum));
  }

  public BigInteger buyAutoPromoteCost(Integer rank, Integer ladderNum) {
    Integer minPeople = Math.max(config.getBaseAssholeLadder(), ladderNum);
    Integer divisor = Math.max(rank - minPeople + 1, 1);

    BigDecimal decGrapes = new BigDecimal(config.getBaseGrapesToBuyAutoPromote());
    return decGrapes.divide(BigDecimal.valueOf(divisor), RoundingMode.FLOOR).toBigInteger();
  }
}
