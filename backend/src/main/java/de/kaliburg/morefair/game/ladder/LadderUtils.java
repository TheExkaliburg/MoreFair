package de.kaliburg.morefair.game.ladder;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundType;
import java.math.BigInteger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LadderUtils {

  private final UpgradeUtils upgradeUtils;
  private final FairConfig config;

  public LadderUtils(UpgradeUtils upgradeUtils, FairConfig config) {
    this.upgradeUtils = upgradeUtils;
    this.config = config;
  }

  /**
   * Return the points the ranker needs on a specific ladder to promote. (including the short wait
   * time in the lead at Rank 1)
   *
   * @param ladder the ladder the ranker is on (and all the other rankers too)
   * @param ranker the ranker from which perspective, these required points are calculated
   * @return the required amount of points
   */
  public BigInteger getPointsForPromoteWithLead(@NonNull LadderEntity ladder,
      @NonNull RankerEntity ranker) {
    if (ladder.getRankers().size() <= 1) {
      // Should always be above the points of the ranker
      return ranker.getPoints().multiply(BigInteger.TWO).max(ladder.getBasePointsToPromote());
    }

    // If not enough points -> minimum required points
    if (ladder.getRankers().get(0).getPoints().compareTo(ladder.getBasePointsToPromote()) < 0) {
      return ladder.getBasePointsToPromote();
    }

    boolean isRankerEqualsToFirstRanker =
        ladder.getRankers().get(0).getUuid().equals(ranker.getUuid());

    // If ladder is before AUTO_PROMOTE_LADDER -> 1st place + 1 points
    if (ladder.getNumber() < config.getAutoPromoteLadder() || ranker.isAutoPromote()) {
      return isRankerEqualsToFirstRanker
          ? ladder.getRankers().get(1).getPoints().add(BigInteger.ONE)
          : ladder.getRankers().get(0).getPoints().add(BigInteger.ONE);
    }

    RankerEntity leadingRanker = isRankerEqualsToFirstRanker ? ranker : ladder.getRankers().get(0);
    RankerEntity pursuingRanker = isRankerEqualsToFirstRanker ? ladder.getRankers().get(1) : ranker;

    BigInteger powerDifference =
        (leadingRanker.isGrowing() ? leadingRanker.getPower() : BigInteger.ZERO)
            .subtract(pursuingRanker.isGrowing() ? pursuingRanker.getPower() : BigInteger.ZERO);

    BigInteger neededPointDifference =
        powerDifference.multiply(BigInteger.valueOf(config.getManualPromoteWaitTime())).abs();

    return (leadingRanker.getUuid().equals(ranker.getUuid()) ? pursuingRanker : leadingRanker)
        .getPoints().add(neededPointDifference).max(ladder.getBasePointsToPromote());
  }

  public Integer getRequiredRankerCountToUnlock(LadderEntity ladder) {
    if (ladder.getRound().getTypes().contains(RoundType.SPECIAL_100)) {
      return config.getBaseAssholeLadder();
    }

    return Math.max(config.getBaseAssholeLadder(), ladder.getScaling());
  }

  /**
   * If following conditions are given the ranker can promote.
   * <ul>
   * <li>Ranker is #1 </li>
   * <li>There are enough people to promote</li>
   * <li>Ranker got enough points to promote </li>
   * <li>Ranker has either:
   *   <ul>
   *      <li>Auto-Promote </li>
   *      <li>enough points to be in front of the next ranker</li></ul>
   * </li>
   * </ul>
   *
   * @param ladder the ladder the ranker is on
   * @param ranker the ranker that gets checked
   * @return if the ranker can promote
   */
  public boolean canPromote(@NonNull LadderEntity ladder, @NonNull RankerEntity ranker) {
    if (!isLadderPromotable(ladder)) {
      return false;
    }

    return ranker.getRank() == 1 && ladder.getRankers().get(0).getUuid().equals(ranker.getUuid())
        && ranker.isGrowing()
        && (ranker.isAutoPromote() || ladder.getTypes().contains(LadderType.FREE_AUTO)
        || ranker.getPoints().compareTo(getPointsForPromoteWithLead(ladder, ranker)) >= 0);
  }

  public boolean isLadderUnlocked(@NonNull LadderEntity ladder) {
    if (ladder.getTypes().contains(LadderType.END)) {
      return false;
    }
    return ladder.getRankers().size() >= getRequiredRankerCountToUnlock(ladder);
  }

  public boolean isLadderPromotable(@NonNull LadderEntity ladder) {
    return isLadderUnlocked(ladder)
        && ladder.getRankers().get(0).getPoints().compareTo(ladder.getBasePointsToPromote()) >= 0;
  }

  /**
   * If following conditions are given the ranker can throw vinegar at the target.
   * <ul>
   *  <li>Target is #1</li>
   *  <li>Target is not you</li>
   *  <li>Target is active on that Ladder</li>
   *  <li>Target does not have auto-promote</li>
   *  <li>There are enough people to promote</li>
   *  <li>Target got enough points to promote</li>
   *  <li>Ranker got enough Vinegar to throw</li>
   * </ul>
   *
   * @param ladder the ladder the vinegar would get thrown on
   * @param ranker the ranker that want to throw vinegar
   * @param target the target of the rankers vinegar-throw
   * @return if the ranker can throw the vinegar
   */
  public boolean canThrowVinegarAt(LadderEntity ladder, RankerEntity ranker, RankerEntity target) {
    if (!isLadderPromotable(ladder)) {
      return false;
    }

    return target.getRank() == 1 && !ranker.getUuid().equals(target.getUuid()) && target.isGrowing()
        && !target.isAutoPromote()
        && ranker.getVinegar().compareTo(upgradeUtils.throwVinegarCost(ladder.getScaling())) >= 0;
  }


  /**
   * If following conditions are given the ranker can buy auto-promote.
   * <ul>
   *  <li>Ranker does not already have auto-promote</li>
   *  <li>Ranker has enough grapes to buy auto-promote</li>
   *  <li>The ladder is not before the auto-promote-ladder</li>
   *  <li>The ladder is not the asshole-ladder</li>
   * </ul>
   *
   * @param ladder the ladder the ranker wants to buy auto-promote on
   * @param ranker the ranker that wants to buy auto-promote
   * @param round  the current round, so we can find out if this ladder is an asshole-ladder
   * @return if the ranker can buy auto-promote
   */
  public boolean canBuyAutoPromote(LadderEntity ladder, RankerEntity ranker, RoundEntity round) {
    BigInteger autoPromoteCost = upgradeUtils.buyAutoPromoteCost(round, ladder, ranker.getRank());

    return !ranker.isAutoPromote()
        && ranker.getGrapes().compareTo(autoPromoteCost) >= 0
        && ladder.getNumber() >= config.getAutoPromoteLadder()
        && ladder.getNumber() < round.getAssholeLadderNumber()
        && !ladder.getTypes().contains(LadderType.NO_AUTO);

  }
}
