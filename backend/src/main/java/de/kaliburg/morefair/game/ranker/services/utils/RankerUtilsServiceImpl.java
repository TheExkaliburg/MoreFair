package de.kaliburg.morefair.game.ranker.services.utils;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ladder.services.utils.LadderUtilsServiceImpl;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import java.math.BigInteger;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankerUtilsServiceImpl implements RankerUtilsService {

  private final LadderService ladderService;
  private final RankerService rankerService;
  private final UpgradeUtils upgradeUtils;
  private final RoundService roundService;
  private final LadderUtilsServiceImpl ladderUtilsService;
  private final FairConfig config;

  /**
   * Return the points the ranker needs on a specific ladder to promote. (including the short wait
   * time in the lead at Rank 1)
   *
   * @param ranker the ranker from which perspective, these required points are calculated
   * @return the required amount of points
   */
  @Override
  public BigInteger getPointsForPromoteWithLead(@NonNull RankerEntity ranker) {
    LadderEntity ladder = ladderService.findLadderById(ranker.getLadderId()).orElseThrow();
    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());

    if (rankers.size() <= 1) {
      // Should always be above the points of the ranker
      return ranker.getPoints().multiply(BigInteger.TWO).max(ladder.getBasePointsToPromote());
    }

    // If not enough points -> minimum required points
    if (rankers.get(0).getPoints().compareTo(ladder.getBasePointsToPromote()) < 0) {
      return ladder.getBasePointsToPromote();
    }

    boolean isRankerEqualsToFirstRanker =
        rankers.get(0).getUuid().equals(ranker.getUuid());

    // If ladder is before AUTO_PROMOTE_LADDER -> 1st place + 1 points
    if (ladder.getNumber() < config.getAutoPromoteLadder() || ranker.isAutoPromote()) {
      return isRankerEqualsToFirstRanker
          ? rankers.get(1).getPoints().add(BigInteger.ONE)
          : rankers.get(0).getPoints().add(BigInteger.ONE);
    }

    RankerEntity leadingRanker = isRankerEqualsToFirstRanker ? ranker : rankers.get(0);
    RankerEntity pursuingRanker = isRankerEqualsToFirstRanker ? rankers.get(1) : ranker;

    BigInteger powerDifference =
        (leadingRanker.isGrowing() ? leadingRanker.getPower() : BigInteger.ZERO)
            .subtract(pursuingRanker.isGrowing() ? pursuingRanker.getPower() : BigInteger.ZERO);

    BigInteger neededPointDifference =
        powerDifference.multiply(BigInteger.valueOf(config.getManualPromoteWaitTime())).abs();

    return (leadingRanker.getUuid().equals(ranker.getUuid()) ? pursuingRanker : leadingRanker)
        .getPoints().add(neededPointDifference).max(ladder.getBasePointsToPromote());
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
   * @param ranker the ranker that gets checked
   * @return if the ranker can promote
   */
  @Override
  public boolean canPromote(@NonNull RankerEntity ranker) {
    LadderEntity ladder = ladderService.findLadderById(ranker.getLadderId()).orElseThrow();

    if (!ladderUtilsService.isLadderPromotable(ladder)) {
      return false;
    }

    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());

    return ranker.getRank() == 1 && rankers.get(0).getUuid().equals(ranker.getUuid())
        && ranker.isGrowing()
        && (ranker.isAutoPromote() || ladder.getTypes().contains(LadderType.FREE_AUTO)
        || ranker.getPoints().compareTo(getPointsForPromoteWithLead(ranker)) >= 0);
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
   * @param ranker the ranker that want to throw vinegar
   * @param target the target of the rankers vinegar-throw
   * @return if the ranker can throw the vinegar
   */
  @Override
  public boolean canThrowVinegarAt(RankerEntity ranker, RankerEntity target, int percentage) {
    LadderEntity ladder = ladderService.findLadderById(ranker.getLadderId()).orElseThrow();

    if (!ladderUtilsService.isLadderPromotable(ladder)) {
      return false;
    }

    BigInteger rankerVinegar = ranker.getVinegar()
        .multiply(BigInteger.valueOf(percentage))
        .divide(BigInteger.valueOf(100));

    return target.getRank() == 1 && !ranker.getUuid().equals(target.getUuid()) && target.isGrowing()
        && !target.isAutoPromote()
        && rankerVinegar.compareTo(upgradeUtils.throwVinegarCost(ladder.getScaling())) >= 0;
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
   * @param ranker the ranker that wants to buy auto-promote
   * @return if the ranker can buy auto-promote
   */
  @Override
  public boolean canBuyAutoPromote(RankerEntity ranker) {
    LadderEntity ladder = ladderService.findLadderById(ranker.getLadderId()).orElseThrow();
    RoundEntity round = roundService.findById(ladder.getRoundId()).orElseThrow();
    BigInteger autoPromoteCost = upgradeUtils.buyAutoPromoteCost(round, ladder, ranker.getRank());

    return !ranker.isAutoPromote()
        && ranker.getGrapes().compareTo(autoPromoteCost) >= 0
        && ladder.getNumber() >= config.getAutoPromoteLadder()
        && ladder.getNumber() < round.getAssholeLadderNumber()
        && !ladder.getTypes().contains(LadderType.NO_AUTO);

  }

  @Override
  public BigInteger getWinningGrapes(LadderEntity ladder) {
    var baseGrapeCostForAuto = config.getBaseGrapesToBuyAutoPromote().intValue();

    float multiplier = 1.f;
    if (ladder.getTypes().contains(LadderType.GENEROUS)) {
      multiplier = 10.f;
    } else if (ladder.getTypes().contains(LadderType.STINGY)) {
      multiplier = .1f;
    }

    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());
    long place = rankers.stream()
        .filter(r -> !r.isGrowing())
        .count() + 1;

    if (place != 1) {
      if (place <= 3) {
        baseGrapeCostForAuto /= 2;
      } else if (place <= 10) {
        baseGrapeCostForAuto /= 10;
      } else {
        baseGrapeCostForAuto = 0;
      }
    }

    return BigInteger.valueOf(Math.round(baseGrapeCostForAuto * multiplier));
  }
}
