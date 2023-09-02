import Decimal from "break_infinity.js";
import { Ranker } from "~/store/entities/ranker";
import { useLadderStore } from "~/store/ladder";
import { useLadderUtils } from "~/composables/useLadderUtils";
import { useStomp } from "~/composables/useStomp";

const etaRankerCache = new Map<number, Map<number, number>>();
const etaPointsCache = new Map<number, Map<Decimal, number>>();
const etaPowerCache = new Map<number, Map<Decimal, number>>();

const computedTrigger = ref<number>(0);

const stomp = useStomp();
stomp.addCallback(stomp.callbacks.onTick, "fair_eta_cache_reset", resetCache);

function resetCache() {
  etaRankerCache.clear();
  etaPointsCache.clear();
  etaPowerCache.clear();
  computedTrigger.value = 1 - computedTrigger.value;
}

function getPowerGainDifferenceToRank(ranker: Ranker, targetRank = 1): Decimal {
  const rank = ranker.rank;
  if (rank < 1 || !isFinite(rank)) return new Decimal(0);

  const powerGainAtStart = ranker.getPowerPerSecond(rank);
  const powerGainAtEnd = ranker.getPowerPerSecond(targetRank + 1);

  return powerGainAtStart.add(powerGainAtEnd).div(new Decimal(2));
}

export const useEta = (ranker: Ranker) => {
  const ladder = useLadderStore();
  const ladderUtils = useLadderUtils();

  function toRanker(target: Ranker): number {
    // To make the computed always check this value, so it recalculates after a tick
    if (computedTrigger.value === Infinity)
      console.error("toRanker", ranker, target);

    let cachedMap = etaRankerCache.get(ranker.accountId);
    if (cachedMap !== undefined) {
      const cachedValue = cachedMap.get(target.accountId);
      if (cachedValue !== undefined) {
        return cachedValue;
      }
    } else {
      cachedMap = new Map<number, number>();
      etaRankerCache.set(ranker.accountId, cachedMap);
    }

    if (!ranker.growing && !target.growing) return Infinity;

    // Calculating the relative acceleration of the two players
    const rankerAcc = getPowerGainDifferenceToRank(
      ranker,
      target.rank < ranker.rank && !target.growing ? target.rank : 1
    );
    const targetAcc = getPowerGainDifferenceToRank(
      target,
      ranker.rank < target.rank && !ranker.growing ? ranker.rank : 1
    );
    const accDiff = targetAcc.sub(rankerAcc);
    // Calculating the relative current speed of the two players
    const rankerSpeed = ranker.growing ? ranker.power : new Decimal(0);
    const targetSpeed = target.growing ? target.power : new Decimal(0);
    const speedDiff = targetSpeed.sub(rankerSpeed);
    // Calculating the current distance between the two players
    const pointsDiff = target.points.sub(ranker.points);
    const result = solveQuadratic(accDiff, speedDiff, pointsDiff).toNumber();

    // saving the value in both maps (target -> ranker and ranker -> target)
    cachedMap.set(target.accountId, result);
    cachedMap = etaRankerCache.get(target.accountId);
    if (cachedMap === undefined) {
      cachedMap = new Map<number, number>();
      etaRankerCache.set(target.accountId, cachedMap);
    }
    cachedMap.set(ranker.accountId, result);
    return result;
  }

  function toPoints(target: Decimal): number {
    // To make the computed always check this value, so it recalculates after a tick
    if (computedTrigger.value === Infinity)
      console.error("toPoints", ranker, target);
    if (target.cmp(ranker.points) <= 0) return 0;

    let cachedMap = etaPointsCache.get(ranker.accountId);
    if (cachedMap !== undefined) {
      const cachedValue = cachedMap.get(target);
      if (cachedValue !== undefined) return cachedValue;
    } else {
      cachedMap = new Map<Decimal, number>();
      etaPointsCache.set(ranker.accountId, cachedMap);
    }

    // We are looking where the number is on the ladder and what the rank would be there, then we set that as target rank
    // If we cannot find the number on the ladder then its under the lowest person
    // if that's the case or the number is already under us, we set the targetRank to 1 above ours
    // this theoretically shouldn't be the case since we check if the points are under us already, but typescripts needs it explicitly
    let targetRank = 1;
    if (ladder.state.rankers.length > 0) {
      const pseudoRanker = ladder.state.rankers.find(
        (r) => r.points.cmp(target) < 0
      );

      if (pseudoRanker === undefined || pseudoRanker.rank >= ranker.rank) {
        targetRank = ranker.rank - 1;
      } else {
        targetRank = pseudoRanker.rank;
      }
    }

    const accDiff = getPowerGainDifferenceToRank(ranker, targetRank).negate();
    const speedDiff = ranker.growing ? ranker.power.negate() : new Decimal(0);
    const pointsDiff = target.sub(ranker.points);
    const result = solveQuadratic(accDiff, speedDiff, pointsDiff).toNumber();
    cachedMap.set(target, result);
    return result;
  }

  function toPower(target: Decimal): number {
    // To make the computed always check this value, so it recalculates after a tick
    if (computedTrigger.value === Infinity)
      console.error("toPoints", ranker, target);
    let cachedMap = etaPowerCache.get(ranker.accountId);
    if (cachedMap !== undefined) {
      const cachedValue = cachedMap.get(target);
      if (cachedValue !== undefined) return cachedValue;
    } else {
      cachedMap = new Map<Decimal, number>();
      etaPowerCache.set(ranker.accountId, cachedMap);
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
    if (ranker.rank === 1 && !ranker.growing) return Infinity;

    const etaRequirement = toPromotionRequirement();

    // We are already first place So we only need to reach the promotion limit.
    if (ranker.rank === 1 || ladder.state.rankers.length <= 1) {
      return etaRequirement;
    }

    // If no one hit the base points requirement already then we show the eta to the requirement.
    if (
      ladder.state.rankers[0].points.cmp(ladder.state.basePointsToPromote) < 0
    ) {
      return etaRequirement;
    }

    // We need to reach the promotion limit and the first place, so we take the max.
    return Math.max(etaRequirement, toFirst() + 30);
  }

  function toVinegarThrow(): number {
    if (!ranker.growing) return 0;

    let secondsToVinegar: Decimal;

    if (
      ranker.rank === ladder.state.rankers.length &&
      ladder.state.rankers.length >= 1
    ) {
      // TODO: When grape modifiers are implemented, acceleration can be changed.
      const acceleration = 2;
      const a = new Decimal(acceleration).div(2);
      const b = ranker.grapes.sub(1);
      const c = ranker.vinegar.sub(ladderUtils.getVinegarThrowCost.value);

      secondsToVinegar = solveQuadratic(a, b, c);
    } else {
      secondsToVinegar =
        ladderUtils.getVinegarThrowCost.value.cmp(ranker.vinegar) >= 0
          ? ladderUtils.getVinegarThrowCost.value
              .sub(ranker.vinegar)
              .div(ranker.grapes)
          : new Decimal(0);
    }

    return secondsToVinegar.toNumber();
  }

  return {
    toRanker,
    toPoints,
    toPower,
    toRank,
    toFirst,
    toPromotionRequirement,
    toPromote,
    toVinegarThrow,
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
