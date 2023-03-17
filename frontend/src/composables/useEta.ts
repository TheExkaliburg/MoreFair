import Decimal from "break_infinity.js";
import { Ranker } from "~/store/entities/ranker";
import { useLadderStore } from "~/store/ladder";
import { useLadderUtils } from "~/composables/useLadderUtils";

export const useEta = (ranker: Ranker) => {
  const ladder = useLadderStore();
  const ladderUtils = useLadderUtils();

  function toRanker(target: Ranker): number {
    // Calculating the relative acceleration of the two players
    const rankerAcc = ranker.getPowerPerSecond();
    const targetAcc = target.getPowerPerSecond();
    const accDiff = targetAcc.sub(rankerAcc);
    // Calculating the relative current speed of the two players
    const rankerSpeed = ranker.growing ? ranker.power : new Decimal(0);
    const targetSpeed = target.growing ? target.power : new Decimal(0);
    const speedDiff = targetSpeed.sub(rankerSpeed);
    // Calculating the current distance between the two players
    const pointsDiff = target.points.sub(ranker.points);
    return solveQuadratic(
      accDiff.div(new Decimal(2)),
      speedDiff,
      pointsDiff
    ).toNumber();
  }

  function toPoints(target: Decimal): number {
    const accDiff = ranker.getPowerPerSecond().negate();
    const speedDiff = ranker.growing ? ranker.power.negate() : new Decimal(0);
    const pointsDiff = target.sub(ranker.points);
    return solveQuadratic(
      accDiff.div(new Decimal(2)),
      speedDiff,
      pointsDiff
    ).toNumber();
  }

  function toPower(target: Decimal): number {
    return target.sub(ranker.power).div(ranker.getPowerPerSecond()).toNumber();
  }

  function toRank(rank: number): number {
    return toRanker(ladder.state.rankers[rank - 1]);
  }

  function toFirst(): number {
    return toRank(1);
  }

  function toPromotionRequirement(): number {
    return toPoints(ladderUtils.getPointsNeededForPromote.value);
  }

  function toPromote(): number {
    const etaRequirement = toPromotionRequirement();

    // We are already first place So we only need to reach the promotion limit.
    if (ranker.rank === 1) {
      return etaRequirement;
    }

    // We need to reach the promotion limit and the first place, so we take the max.
    return Math.max(etaRequirement, toFirst() + 30);
  }

  return {
    toRanker,
    toPoints,
    toPower,
    toRank,
    toFirst,
    toPromotionRequirement,
    toPromote,
  };
};

function solveQuadratic(
  accelerationDiff: Decimal,
  speedDiff: Decimal,
  pointDiff: Decimal
): Decimal {
  // IF Acceleration is equal, only check speed
  if (accelerationDiff.eq_tolerance(new Decimal(0), Number.EPSILON)) {
    const flatTime = pointDiff.negate().div(speedDiff);
    return flatTime.cmp(new Decimal(0)) > 0
      ? flatTime
      : new Decimal(Number.POSITIVE_INFINITY);
  }

  // b^2 - 4ac
  const discriminant = speedDiff
    .pow(2)
    .sub(accelerationDiff.mul(pointDiff).mul(4));
  if (discriminant.cmp(new Decimal(0)) < 0)
    return new Decimal(Number.POSITIVE_INFINITY);

  // -b +- sqrt(b^2 - 4ac) / 2a
  const root1 = speedDiff
    .negate()
    .add(discriminant.sqrt())
    .div(accelerationDiff.mul(new Decimal(2)));
  const root2 = speedDiff
    .negate()
    .sub(discriminant.sqrt())
    .div(accelerationDiff.mul(new Decimal(2)));

  if (root1.cmp(new Decimal(0)) > 0 && root2.cmp(new Decimal(0)) > 0) {
    return root1.min(root2);
  }

  const maxRoot = root1.max(root2);
  if (maxRoot.cmp(new Decimal(0)) < 0)
    return new Decimal(Number.POSITIVE_INFINITY);
  else return maxRoot;
}
