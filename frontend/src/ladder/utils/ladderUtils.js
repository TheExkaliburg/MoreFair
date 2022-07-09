import Decimal from "break_infinity.js";

export default {
  getMinimumPointsForPromote(settings, ladder) {
    return settings.pointsForPromote.mul(new Decimal(ladder.number));
  },
  getMinimumPeopleForPromote(settings, ladder) {
    return Math.max(settings.minimumPeopleForPromote, ladder.number);
  },
  getAutoPromoteCost(settings, ladder, rank) {
    let minPeople = this.getMinimumPeopleForPromote(settings, ladder);
    let divisor = Math.max(rank - minPeople + 1, 1);
    return settings.baseGrapesNeededToAutoPromote.div(divisor).floor();
  },
  isLadderUnlocked(settings, ladder) {
    if (ladder.rankers.length <= 0) return false;
    let rankerCount = ladder.rankers.length;
    if (rankerCount < this.getMinimumPeopleForPromote(settings, ladder))
      return false;
    return (
      ladder.rankers[0].points.cmp(
        this.getMinimumPointsForPromote(settings, ladder)
      ) >= 0
    );
  },
  canThrowVinegar(settings, ladder) {
    return (
      ladder.rankers[0].growing &&
      !ladder.rankers[0].you &&
      ladder.rankers[0].points.cmp(
        this.getMinimumPointsForPromote(settings, ladder)
      ) >= 0 &&
      ladder.rankers.length >=
        this.getMinimumPeopleForPromote(settings, ladder) &&
      ladder.yourRanker.vinegar.cmp(
        this.getVinegarThrowCost(settings, ladder)
      ) >= 0
    );
  },
  getVinegarThrowCost(settings, ladder) {
    return settings.baseVinegarNeededToThrow.mul(new Decimal(ladder.number));
  },
  getNextUpgradeCost(ladder, currentUpgrade) {
    return new Decimal(
      Math.round(Math.pow(ladder.number + 1, currentUpgrade + 1))
    );
  },
};
