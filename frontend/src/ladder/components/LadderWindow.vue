<template>
  <div class="row py-1">
    <PaginationGroup
      :current="ladder.ladderNumber"
      :max="user.highestCurrentLadder"
      :onChange="changeLadder"
    />
  </div>
  <div class="row py-1 ladder-row">
    <table
      class="table table-sm caption-top table-borderless"
      style="border: 0px solid yellow"
    >
      <thead>
        <tr class="thead-light">
          <th class="col-1 text-start">-</th>
          <th class="col-5 text-start">Username</th>
          <th class="col-3 text-end">Power</th>
          <th class="col-3 text-end">Points</th>
        </tr>
      </thead>
      <tbody id="ladderBody" class="">
        <tr
          v-for="ranker in rankers"
          :key="ranker.accountId"
          :class="[ranker.you ? 'you' : '', ranker.growing ? '' : 'promoted']"
        >
          <td class="text-start">{{ ranker.rank }}</td>
          <td class="text-start">{{ ranker.username }}</td>
          <td class="text-end">
            {{ numberFormatter.format(ranker.power) }} [+{{
              ("" + ranker.bias).padStart(2, "0")
            }}
            x{{ ("" + ranker.multiplier).padStart(2, "0") }}]
          </td>
          <td class="text-end">
            {{ numberFormatter.format(ranker.points) }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject } from "vue";
import PaginationGroup from "@/components/PaginationGroup";

const store = useStore();
const stompClient = inject("$stompClient");

const numberFormatter = computed(() => store.state.numberFormatter);
const ladder = computed(() => store.state.ladder.ladder);
const user = computed(() => store.state.user);
const rankers = computed(() => store.getters["ladder/shownRankers"]);
const allRankers = computed(() => store.getters["ladder/allRankers"]);

function changeLadder(event) {
  const targetLadder = event.target.dataset.number;

  if (targetLadder !== ladder.value.ladderNumber) {
    stompClient.unsubscribe("/topic/ladder/" + ladder.value.ladderNumber);
    stompClient.subscribe("/topic/ladder/" + targetLadder, (message) => {
      store.dispatch({
        type: "ladder/update",
        message: message,
        stompClient: stompClient,
      });
    });
    stompClient.send("/app/ladder/init/" + targetLadder);
  }
}

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
      const p1Acceleration = powerPerSecond(ranker);
      const accelerationDiff = -p1Acceleration;

      const p1Speed = ranker.growing ? ranker.power : 0;
      const speedDiff = -p1Speed;

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
      return (ranker.power - power) / powerPerSecond(ranker);
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

  if (hours == 0 && minutes == 0 && seconds !== 0 && Math.abs(seconds) < 1) {
    return "<1s";
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
  return rankers.value[i];
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
@import "../../styles/styles";

.rank {
  padding-left: 1rem;
}

.ladder-row {
  height: 50%;
  max-height: 50%;
  overflow-y: auto;
  align-content: start;

  thead {
    background-color: $background-color;
    position: sticky;
    top: 0;
  }

  tbody {
    overflow: auto;
  }
}

.dropdown-pagination {
  text-align: end;
  padding-right: 0px;
}

.you {
  background-color: $button-color;
  color: $button-hover-color;
}

.promoted {
  background-color: #1e1d39;
}
</style>
