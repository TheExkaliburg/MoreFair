package de.kaliburg.morefair.game;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.services.utils.LadderUtilsService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpgradeUtils {

  private static final BigDecimal LADDER_UPGRADE_MULTIPLIER = BigDecimal.valueOf(0.5);
  private static final BigDecimal FLAT_UPGRADE_MULTIPLIER = BigDecimal.valueOf(0.5);

  private final FairConfig config;
  private final LadderUtilsService ladderUtils;

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
    BigDecimal flatMulti = BigDecimal.ONE;
    BigDecimal ladderMulti = BigDecimal.ONE;
    if (ladderTypes.contains(LadderType.CHEAP)) {
      flatMulti = BigDecimal.ONE.subtract(FLAT_UPGRADE_MULTIPLIER);
      ladderMulti = BigDecimal.ONE.subtract(LADDER_UPGRADE_MULTIPLIER);
    }
    if (ladderTypes.contains(LadderType.EXPENSIVE)) {
      flatMulti = BigDecimal.ONE.add(FLAT_UPGRADE_MULTIPLIER);
      ladderMulti = BigDecimal.ONE.add(LADDER_UPGRADE_MULTIPLIER);
    }

    BigDecimal ladder = BigDecimal.valueOf(ladderNumber);
    ladder = ladder.multiply(ladderMulti).add(BigDecimal.ONE);
    BigDecimal result = ladder.pow(currentUpgrade + 1).multiply(flatMulti);
    // adding 0.5 to round at 0.5, otherwise it would always round down
    // also clamping the lower bound at 1
    return BigInteger.ONE.max(result.add(BigDecimal.valueOf(0.5)).toBigInteger());
  }

  /**
   * Calculates the cost to throw vinegar, based on the round type and the given ladder number. If
   * the round contains the APRIL_FOOLS type, it returns a fixed base cost. Otherwise, it multiplies
   * the base vinegar cost by the specified ladder number.
   *
   * @param round     the round entity containing details about the current game round
   * @param ladderNum the number of the ladder for which the vinegar cost is being calculated
   * @return the total vinegar cost as a BigInteger
   */
  public BigInteger throwVinegarCost(RoundEntity round, Integer ladderNum) {
    if (round.getTypes().contains(RoundType.APRIL_FOOLS)) {
      return config.getBaseVinegarToThrow();
    }
    return config.getBaseVinegarToThrow().multiply(BigInteger.valueOf(ladderNum));
  }

  public BigInteger buyAutoPromoteCost(RoundEntity round, LadderEntity ladder, Integer rank) {
    Integer minPeople = ladderUtils.getRequiredRankerCountToUnlock(ladder);

    int divisor = Math.max(rank - minPeople + 1, 1);

    BigDecimal decGrapes = new BigDecimal(config.getBaseGrapesToBuyAutoPromote());
    return decGrapes.divide(BigDecimal.valueOf(divisor), RoundingMode.FLOOR).toBigInteger();
  }
}
