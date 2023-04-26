import Decimal from "break_infinity.js";
import { Ranker } from "~/store/entities/ranker";
import { useLadderStore } from "~/store/ladder";
import { useLadderUtils } from "~/composables/useLadderUtils";
import { useStomp } from "~/composables/useStomp";

let countCacheUses = 0;
let countTotalUses = 0;
const etaRankerCache = new Map<Ranker, Map<Ranker, number>>();
const etaPointsCache = new Map<Ranker, Map<Decimal, number>>();
const etaPowerCache = new Map<Ranker, Map<Decimal, number>>();

const stomp = useStomp();
stomp.addCallback(stomp.callbacks.onTick, "fair_eta_cache_reset", resetCache);

function resetCache() {
  console.log(
    "Resetting Eta Cache; total:",
    countTotalUses,
    "cache:",
    countCacheUses,
    "new:",
    countTotalUses - countCacheUses
  );

  countTotalUses = 0;
  countCacheUses = 0;

  etaRankerCache.clear();
  etaPointsCache.clear();
  etaPowerCache.clear();
}

export const useEta = (ranker: Ranker) => {
  const ladder = useLadderStore();
  const ladderUtils = useLadderUtils();

  function toRanker(target: Ranker): number {
    countTotalUses++;
    let cachedMap = etaRankerCache.get(ranker);
    if (cachedMap !== undefined) {
      const cachedValue = cachedMap.get(target);
      if (cachedValue !== undefined) {
        countCacheUses++;
        return cachedValue;
      }
    } else {
      cachedMap = new Map<Ranker, number>();
      etaRankerCache.set(ranker, cachedMap);
    }

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
    const result = solveQuadratic(
      accDiff.div(new Decimal(2)),
      speedDiff,
      pointsDiff
    ).toNumber();

    // saving the value in both maps (target -> ranker and ranker -> target)
    cachedMap.set(target, result);
    cachedMap = etaRankerCache.get(target);
    if (cachedMap === undefined) {
      cachedMap = new Map<Ranker, number>();
      etaRankerCache.set(target, cachedMap);
    }
    cachedMap.set(ranker, result);

    return result;
  }

  function toPoints(target: Decimal): number {
    if (target.cmp(ranker.points) <= 0) return 0;

    let cachedMap = etaPointsCache.get(ranker);
    if (cachedMap !== undefined) {
      const cachedValue = cachedMap.get(target);
      if (cachedValue !== undefined) return cachedValue;
    } else {
      cachedMap = new Map<Decimal, number>();
      etaPointsCache.set(ranker, cachedMap);
    }

    const accDiff = ranker.getPowerPerSecond().negate();
    const speedDiff = ranker.growing ? ranker.power.negate() : new Decimal(0);
    const pointsDiff = target.sub(ranker.points);
    const result = solveQuadratic(
      accDiff.div(new Decimal(2)),
      speedDiff,
      pointsDiff
    ).toNumber();
    cachedMap.set(target, result);
    return result;
  }

  function toPower(target: Decimal): number {
    let cachedMap = etaPowerCache.get(ranker);
    if (cachedMap !== undefined) {
      const cachedValue = cachedMap.get(target);
      if (cachedValue !== undefined) return cachedValue;
    } else {
      cachedMap = new Map<Decimal, number>();
      etaPowerCache.set(ranker, cachedMap);
    }

    const result = target
      .sub(ranker.power)
      .div(ranker.getPowerPerSecond())
      .toNumber();
    cachedMap.set(target, result);
    return result;
  }

  function toRank(rank: number): number {
    return toRanker(ladder.state.rankers[rank - 1]);
  }

  function toFirst(): number {
    return toRank(1);
  }

  function toPromotionRequirement(): number {
    if (ladder.getters.yourRanker?.accountId === ranker.accountId)
      return toPoints(ladderUtils.getYourPointsNeededToPromote.value);

    return toPoints(ladderUtils.getPointsNeededToPromote(ranker));
  }

  function toPromote(): number {
    const etaRequirement = toPromotionRequirement();

    // We are already first place So we only need to reach the promotion limit.
    if (ranker.rank === 1 || ladder.state.rankers.length <= 1) {
      return etaRequirement;
    }

    // If no one hit the base points requirement already then we show the eta to the first place.
    if (
      ladder.state.rankers[0].points.cmp(ladder.state.basePointsToPromote) < 0
    ) {
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
