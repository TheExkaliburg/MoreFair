package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class UpgradeUtils {

  private static final BigDecimal LADDER_UPGRADE_MULTIPLIER = BigDecimal.valueOf(0.5);
  private static final BigDecimal FLAT_UPGRADE_MULTIPLIER = BigDecimal.valueOf(0.5);

  private final FairConfig config;

  private LadderUtils ladderUtils;

  public UpgradeUtils(FairConfig config, @Lazy LadderUtils ladderUtils) {
    this.config = config;
    this.ladderUtils = ladderUtils;
  }

  void setLadderUtils(LadderUtils ladderUtils) {
    this.ladderUtils = ladderUtils;
  }

  /**
   * Calculates the default cost of the next bias/multi upgrade.
   *
   * <p><code>cost = (ladder + 1) ^ (currentUpgrade + 1)</code>
   *
   * @param currentUpgrade the current amount of upgrades
   * @param ladderNumber   the ladder where you would buy the upgrade
   * @return the cost of the (currentUpgrade + 1)th bias/multi
   */
  public BigInteger buyUpgradeCost(Integer ladderNumber, Integer currentUpgrade) {
    return buyUpgradeCost(ladderNumber, currentUpgrade, EnumSet.of(LadderType.DEFAULT));
  }

  /**
   * Calculates the cost of the next bias/multi upgrade.
   *
   * <p><code>cost = (ladder + 1) ^ (currentUpgrade + 1)</code>
   * <p/>
   * According to the CHEAP and EXPENSIVE ladder type there are 3 things changing in this formula:
   * <ul>
   *   <li>the ladder number is adjusted by 20%</li>
   *   <li>the currentUpgrade is adjusted by 1</li>
   *   <li>the result is adjusted by 50%</li>
   * </ul>
   *
   * @param currentUpgrade the current amount of upgrades
   * @param ladderNumber   the ladder where you would buy the upgrade
   * @param ladderTypes    the types of ladders where you would buy the upgrade
   * @return the cost of the (currentUpgrade + 1)th bias/multi
   */
  public BigInteger buyUpgradeCost(Integer ladderNumber, Integer currentUpgrade,
      Set<LadderType> ladderTypes) {
    if (currentUpgrade <= 0) {
      currentUpgrade = 0;
    }

    BigDecimal flatMulti = BigDecimal.ONE;
    BigDecimal ladderMulti = BigDecimal.ONE;
    if (ladderTypes.contains(LadderType.CHEAP)) {
      flatMulti = flatMulti.subtract(FLAT_UPGRADE_MULTIPLIER);
      ladderMulti = ladderMulti.subtract(LADDER_UPGRADE_MULTIPLIER);
    }

    if (ladderTypes.contains(LadderType.EXPENSIVE)) {
      flatMulti = flatMulti.add(FLAT_UPGRADE_MULTIPLIER);
      ladderMulti = ladderMulti.add(LADDER_UPGRADE_MULTIPLIER);
    }

    BigDecimal ladder = BigDecimal.valueOf(ladderNumber);
    ladder = ladder.multiply(ladderMulti);
    if (ladderTypes.contains(LadderType.CHEAP_2)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_3)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_4)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_5)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_6)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_7)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_8)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_9)) {
      ladder = ladder.multiply(ladderMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_10)) {
      ladder = ladder.multiply(ladderMulti);
    }

    BigDecimal result = ladder.add(BigDecimal.ONE).pow(currentUpgrade + 1).multiply(flatMulti);
    if (ladderTypes.contains(LadderType.CHEAP_2)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_3)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_4)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_5)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_6)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_7)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_8)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_9)) {
      result = result.multiply(flatMulti);
    }
    if (ladderTypes.contains(LadderType.CHEAP_10)) {
      result = result.multiply(flatMulti);
    }

    // adding 0.5 to round at 0.5, otherwise it would always round down
    // also clamping the lower bound at 1
    return BigInteger.ONE.max(result.add(BigDecimal.valueOf(0.5)).toBigInteger());
  }

  public BigInteger throwVinegarCost(Integer ladderNum) {
    return config.getBaseVinegarToThrow().multiply(BigInteger.valueOf(ladderNum));
  }

  public BigInteger buyAutoPromoteCost(RoundEntity round, LadderEntity ladder, Integer rank) {
    Integer minPeople = ladderUtils.getRequiredRankerCountToUnlock(ladder);

    Integer divisor = Math.max(rank - minPeople + 1, 1);

    BigDecimal decGrapes = new BigDecimal(config.getBaseGrapesToBuyAutoPromote());
    return decGrapes.divide(BigDecimal.valueOf(divisor), RoundingMode.FLOOR).toBigInteger();
  }
}
