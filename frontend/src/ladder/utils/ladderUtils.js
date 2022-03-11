import Decimal from "break_infinity.js";

class LadderUtils {
  static getNextUpgradeCost(currentUpgrade, ladderNumber) {
    return new Decimal(
      Math.round(Math.pow(ladderNumber + 1, currentUpgrade + 1))
    );
  }

  static canThrowVinegar(ladder, settings) {
    return (
      ladder.firstRanker.growing &&
      !ladder.firstRanker.you &&
      ladder.firstRanker.points.cmp(settings.pointsForPromote) >= 0 &&
      ladder.rankers.length >= settings.minimumPeopleForPromote &&
      ladder.yourRanker.vinegar.cmp(ladder.getVinegarThrowCost()) >= 0
    );
  }
}

export default LadderUtils;
