import { computed } from "vue";
import store from "../store";
import Decimal from "break_infinity.js";

/**
 * @typedef {import("break_infinity.js").DecimalSource} DecimalSource
 */

const allRankers = computed(() => store.getters["ladder/allRankers"]);

export function eta(ranker) {
  function powerPerSecond(ranker) {
    if (ranker === undefined) {
      throw new Error("ranker is undefined");
    }
    if (ranker.rank === 1 || !ranker.growing) return 0;
    return (ranker.bias + ranker.rank - 1) * ranker.multiplier;
  }
  return {
    /**
     * @param {Ranker} ranker2 - ranker to reach
     * @returns {number} - seconds
     */
    toRanker: (ranker2) => {
      //Calculating the relative acceleration of the two players
      const p1Acceleration = powerPerSecond(ranker);
      const p2Acceleration = powerPerSecond(ranker2);
      const accelerationDiff = p2Acceleration - p1Acceleration;

      //Calculating the relative current speed of the two players
      const p1Speed = ranker.growing ? ranker.power : 0;
      const p2Speed = ranker2.growing ? ranker2.power : 0;
      const speedDiff = p2Speed - p1Speed;

      //Calculating the current distance between the two players
      const p1Points = ranker.points;
      const p2Points = ranker2.points;
      const pointsDiff = p2Points - p1Points;

      const timeLeftInSeconds = solveQuadratic(
        accelerationDiff / 2,
        speedDiff,
        pointsDiff
      );
      return timeLeftInSeconds;
    },
    /**
     * @param {DecimalSource} points - points to reach
     * @returns {number} - seconds
     */
    toPoints: (points) => {
      //To calculate the time to reach a certain point, we pretend to ty to catch up to a ranker that is not growing and has the exact points we want to reach
      const accelerationDiff = -powerPerSecond(ranker);
      const speedDiff = ranker.growing ? -ranker.power : 0;
      const p1Points = ranker.points;
      const p2Points = new Decimal(points);
      const pointsDiff = p2Points.sub(p1Points);

      const timeLeftInSeconds = solveQuadratic(
        accelerationDiff,
        speedDiff,
        pointsDiff
      );
      return timeLeftInSeconds;
    },
    /**
     * @param {DecimalSource} power - power to reach
     * @returns {number} - seconds
     */
    toPower: (power) => {
      //A bit weird, but ranker.power is guaranteed to be a Decimal object and power might not be.
      power = new Decimal(power);
      return power.sub(ranker.power).div(powerPerSecond(ranker));
    },
    /**
     * @param {number} rank - rank to reach
     * @returns {number} - seconds
     */
    toRank: (rank) => {
      for (let i = 0; i < allRankers.value.length; i++) {
        if (allRankers.value[i].rank === rank) {
          return eta(ranker).toRanker(allRankers.value[i]);
        }
      }
    },
    /**
     * @returns {number} - seconds to reach the first rank
     */
    toFirst: () => {
      return eta(ranker).toRank(1);
    },
  };
}

function solveQuadratic(accelerationDiff, speedDiff, pointDiff) {
  if (accelerationDiff == 0) {
    return -pointDiff / speedDiff > 0
      ? -pointDiff / speedDiff
      : Number.POSITIVE_INFINITY;
  } else {
    let discriminant = speedDiff * speedDiff - 4 * accelerationDiff * pointDiff;
    if (discriminant < 0) return Number.POSITIVE_INFINITY;
    const root1 =
      (-speedDiff + Math.sqrt(discriminant)) / (2 * accelerationDiff);
    const root2 =
      (-speedDiff - Math.sqrt(discriminant)) / (2 * accelerationDiff);
    if (root1 > 0 && root2 > 0) {
      return Math.min(root1, root2);
    } else {
      let maxRoot = Math.max(root1, root2);
      if (maxRoot < 0) return Number.POSITIVE_INFINITY;
      else return maxRoot;
    }
  }
}
