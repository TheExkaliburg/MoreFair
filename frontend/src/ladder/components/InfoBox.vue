<template>
  <div class="row py-1">
    <div class="col-6">
      <div class="container px-3">
        <div class="row py-0">
          <div class="col px-0 text-start">
            {{ numberFormatter.format(yourRanker.grapes)
            }}<span v-if="ladder.ladderNumber >= settings.autoPromoteLadder"
              >/<span class="text-highlight">{{
                numberFormatter.format(
                  ladder.getAutoPromoteCost(yourRanker.rank, settings)
                )
              }}</span></span
            >
            Grapes <span v-if="yourRanker.autoPromote">(Active)</span>
          </div>
          <div class="col px-0 text-end">
            {{ numberFormatter.format(yourRanker.vinegar) }}/<span
              class="text-highlight"
              >{{
                numberFormatter.format(ladder.getVinegarThrowCost(settings))
              }}</span
            >
            Vinegar
          </div>
        </div>
        <div class="row py-0">
          <div class="col px-0 btn-group">
            <button
              :class="[
                !yourRanker.autoPromote &&
                !autoPromoteLastSecond &&
                yourRanker.grapes.cmp(
                  ladder.getAutoPromoteCost(yourRanker.rank, settings)
                ) >= 0
                  ? ''
                  : 'disabled',
                ladder.ladderNumber >= settings.autoPromoteLadder ? '' : 'hide',
              ]"
              class="btn btn-outline-primary shadow-none"
              @click="buyAutoPromote"
            >
              Buy Autopromote
            </button>
            <button
              :class="canThrowVinegar && !vinegarLastSecond ? '' : 'disabled'"
              class="btn btn-outline-primary shadow-none"
              @click="throwVinegar"
            >
              Throw Vinegar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="col-6 px-0">
      <div class="container px-3">
        <div class="row py-0">
          <div class="col px-0 text-start">
            {{ numberFormatter.format(yourRanker.power) }}/<span
              class="text-highlight"
              >{{ numberFormatter.format(multiCost) }}
            </span>
            Power
          </div>
          <div class="col px-0 text-end">
            {{ numberFormatter.format(yourRanker.points) }}/<span
              class="text-highlight"
              >{{ numberFormatter.format(biasCost) }}
            </span>
            Points
          </div>
        </div>
        <div class="row py-0">
          <div class="col px-0 btn-group">
            <button
              :class="
                yourRanker.power.cmp(multiCost) >= 0 && !multiLastSecond
                  ? ''
                  : 'disabled'
              "
              class="btn btn-outline-primary shadow-none"
              @click="buyMulti"
            >
              +1 Multi
              {{
                yourRanker.power.cmp(multiCost) >= 0
                  ? ""
                  : `(${secondsToHms(eta(yourRanker).toPower(multiCost))})`
              }}
            </button>
            <button
              :class="
                yourRanker.points.cmp(biasCost) >= 0 && !biasLastSecond
                  ? ''
                  : 'disabled'
              "
              class="btn btn-outline-primary shadow-none"
              @click="buyBias"
            >
              +1 Bias
              {{
                yourRanker.points.cmp(biasCost) >= 0
                  ? ""
                  : `(${secondsToHms(eta(yourRanker).toPoints(biasCost))})`
              }}
            </button>
          </div>
        </div>
        <br />
        <div class="row py-0 text-start">
          {{
            ladder.ladderNumber >= settings.assholeLadder
              ? "Be Asshole"
              : "Promote"
          }}
          at
          {{ numberFormatter.format(stats.pointsNeededForManualPromote) }}
          Points ({{
            numberFormatter.format(
              yourRanker.points.mul(100).div(stats.pointsNeededForManualPromote)
            )
          }}%)
          {{
            yourRanker.points.cmp(stats.pointsNeededForManualPromote) >= 0
              ? ""
              : `(${secondsToHms(
                  eta(yourRanker).toFirst() * 2 // TODO: fix this, it's wrong. But the number seems to be half of the actual time. I don't know why.
                )})`
          }}
        </div>
        <div class="row py-0">
          <button
            :class="[
              yourRanker.points.cmp(stats.pointsNeededForManualPromote) >= 0
                ? ''
                : 'hide',
              !promoteLastSecond ? '' : 'disabled',
            ]"
            class="btn btn-outline-primary shadow-none"
            @click="promote"
          >
            {{
              ladder.ladderNumber >= settings.assholeLadder
                ? "Be Asshole"
                : "Promote"
            }}
          </button>
        </div>
      </div>
    </div>
    <div class="col-6"></div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, ref } from "vue";

const store = useStore();
const stompClient = inject("$stompClient");

// variables
const biasLastSecond = ref(false);
const multiLastSecond = ref(false);
const vinegarLastSecond = ref(false);
const autoPromoteLastSecond = ref(false);
const promoteLastSecond = ref(false);

// computed
const ladder = computed(() => store.state.ladder.ladder);
const stats = computed(() => store.state.ladder.stats);
const settings = computed(() => store.state.settings);
const numberFormatter = computed(() => store.state.numberFormatter);
const yourRanker = computed(() => ladder.value.yourRanker);
const biasCost = computed(() =>
  ladder.value.getNextUpgradeCost(yourRanker.value.bias)
);
const allRankers = computed(() => store.getters["ladder/allRankers"]);

const multiCost = computed(() =>
  ladder.value.getNextUpgradeCost(yourRanker.value.multiplier)
);

const canThrowVinegar = computed(() =>
  ladder.value.canThrowVinegar(settings.value)
);

function throwVinegar(event) {
  vinegarLastSecond.value = true;
  setTimeout(() => (vinegarLastSecond.value = false), 1000);
  stompClient.send("/app/ladder/post/vinegar", { event: event });
}

function buyAutoPromote(event) {
  autoPromoteLastSecond.value = true;
  setTimeout(() => (autoPromoteLastSecond.value = false), 1000);
  stompClient.send("/app/ladder/post/auto-promote", { event: event });
}

function buyBias(event) {
  biasLastSecond.value = true;
  setTimeout(() => (biasLastSecond.value = false), 1000);
  stompClient.send("/app/ladder/post/bias", { event: event });
}

function buyMulti(event) {
  multiLastSecond.value = true;
  setTimeout(() => (multiLastSecond.value = false), 1000);
  stompClient.send("/app/ladder/post/multi", { event: event });
}

function promote(event) {
  promoteLastSecond.value = true;
  setTimeout(() => (promoteLastSecond.value = false), 1000);
  if (ladder.value.ladderNumber >= settings.value.assholeLadder) {
    if (confirm("Do you really wanna be an Asshole?!"))
      stompClient.send("/app/ladder/post/asshole", { event: event });
  } else {
    stompClient.send("/app/ladder/post/promote", { event: event });
  }
}

// TODO: Remove Difference in asshole-event and promote

/**
 * Typedefs for stuff used here
 * @typedef {import('../entities/ranker.js').default} Ranker
 */

/**
 * @param {Ranker} ranker
 */
function eta(ranker) {
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
        accelerationDiff,
        speedDiff,
        pointsDiff
      );
      return timeLeftInSeconds;
    },
    /**
     * @param {number} points - points to reach
     * @returns {number} - seconds
     */
    toPoints: (points) => {
      //To calculate the time to reach a certain point, we pretend to ty to catch up to a ranker that is not growing and has the exact points we want to reach
      const accelerationDiff = -powerPerSecond(ranker);
      const speedDiff = ranker.growing ? -ranker.power : 0;
      const p1Points = ranker.points;
      const p2Points = points;
      const pointsDiff = p2Points - p1Points;

      const timeLeftInSeconds = solveQuadratic(
        accelerationDiff,
        speedDiff,
        pointsDiff
      );
      return timeLeftInSeconds;
    },
    /**
     * @param {number} power - power to reach
     * @returns {number} - seconds
     */
    toPower: (power) => {
      return (power - ranker.power) / powerPerSecond(ranker);
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
window.eta = eta;

function secondsToHms(s) {
  if (s === 0) return "0s";
  if (!Number.isFinite(s)) return "∞";
  const negative = s < 0;
  if (negative) s = -s;
  let hours = Math.floor(s / 3600);
  let minutes = Math.floor((s % 3600) / 60);
  let seconds = (s % 3600) % 60;

  if (hours == 0 && minutes == 0 && seconds !== 0) {
    if (Math.abs(seconds) < 1) {
      return "<1s";
    }
    return `${negative ? "-" : ""}${Math.floor(seconds)}s`;
  }

  let mayPadd = false;
  const padd = (num, suff) => {
    if (num == 0 && !mayPadd) return "";
    if (num < 10 && mayPadd) {
      return "0" + num + suff;
    }
    mayPadd = true;
    return num + suff;
  };

  seconds = Math.floor(seconds);

  return (
    (negative ? "-" : "") +
    padd(hours, ":", false) +
    padd(minutes, ":") +
    padd(seconds, "")
  );
}

window.secondsToHms = secondsToHms;
window.allRankers = allRankers;
window.rankers = (i) => {
  return allRankers.value[i];
};

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
</script>

<style lang="scss" scoped>
div .row {
  height: 50%;
}

div .col-6 {
  font-size: 11px;
  text-align: start;
  height: 50%;
  // border: white solid 1px;
}

.hide {
  display: none !important;
}
</style>
