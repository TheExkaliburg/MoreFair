<template>
  <div class="row py-1">
    <div class="col-6">
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
          <div class="col px-0 btn-group d-flex">
            <button
              :class="isMultiEnabled ? '' : 'disabled'"
              class="btn btn-outline-primary shadow-none w-100"
              @click="buyMulti"
            >
              <span v-if="isMultiEnabled">+1 Multi</span>
              <span v-else>+M ({{ secondsToHms(etaMulti) }})</span>
            </button>
            <button
              :class="isBiasEnabled ? '' : 'disabled'"
              class="btn btn-outline-primary shadow-none w-100"
              @click="buyBias"
            >
              <span v-if="isBiasEnabled">+1 Bias</span>
              <span v-else>+B ({{ secondsToHms(etaBias) }})</span>
            </button>
          </div>
        </div>
        <br />
        <div class="row py-0">
          <div class="col px-0 text-start">
            {{
              hideVinegarAndGrapes
                ? "[[Hidden]]"
                : numberFormatter.format(yourRanker.grapes)
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
            {{
              hideVinegarAndGrapes
                ? "[[Hidden]]"
                : numberFormatter.format(yourRanker.vinegar)
            }}/<span class="text-highlight">{{
                numberFormatter.format(ladder.getVinegarThrowCost(settings))
            }}</span>
            Vinegar
          </div>
        </div>
        <div class="row py-0">
          <div class="col px-0 btn-group d-flex">
            <button
              v-if="ladder.ladderNumber !== settings.assholeLadder"
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
              class="btn btn-outline-primary shadow-none w-100"
              @click="buyAutoPromote"
            >
              Buy Autopromote
            </button>
            <button
              v-else-if="ladder.ladderNumber === settings.assholeLadder"
              :class="[
                !promoteLastSecond &&
                yourRanker.points.cmp(stats.pointsNeededForManualPromote) >= 0
                  ? ''
                  : 'disabled',
              ]"
              class="btn btn-outline-primary shadow-none w-100"
              @click="promote"
            >
              Be Asshole
            </button>
            <button
              v-if="yourRanker.rank !== 1"
              :class="canThrowVinegar && !vinegarLastSecond ? '' : 'disabled'"
              class="btn btn-outline-primary shadow-none w-100"
              @click="throwVinegar"
            >
              Throw Vinegar
            </button>
            <button
              v-else-if="
                yourRanker.rank === 1 &&
                ladder.ladderNumber !== settings.assholeLadder
              "
              :class="[
                !promoteLastSecond &&
                yourRanker.points.cmp(stats.pointsNeededForManualPromote) >= 0
                  ? ''
                  : 'disabled',
              ]"
              class="btn btn-outline-primary shadow-none w-100"
              @click="promote"
            >
              Promote
            </button>
          </div>
        </div>
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
              : `(${secondsToHms(eta(yourRanker).toFirst())})`
          }}
        </div>
      </div>
    </div>

    <div class="col-6"></div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, ref } from "vue";
import { eta } from "../../modules/eta";
import { secondsToHms } from "../../modules/formatting";

const store = useStore();
const stompClient = inject("$stompClient");

// variables

// Disables a button if its pressed in the last second
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

const hideVinegarAndGrapes = computed(() =>
  store.getters["options/getOptionValue"]("hideVinAndGrapeCount")
);

const biasCost = computed(() =>
  ladder.value.getNextUpgradeCost(yourRanker.value.bias)
);
//const allRankers = computed(() => store.getters["ladder/allRankers"]);

const multiCost = computed(() =>
  ladder.value.getNextUpgradeCost(yourRanker.value.multiplier)
);

const canThrowVinegar = computed(() =>
  ladder.value.canThrowVinegar(settings.value)
);

const isBiasEnabled = computed(
  () =>
    yourRanker.value.points.cmp(biasCost.value) >= 0 && !biasLastSecond.value
);
const isMultiEnabled = computed(
  () =>
    yourRanker.value.power.cmp(multiCost.value) >= 0 && !multiLastSecond.value
);
//const isAutoPromoteEnabled = computed(() => false);
//const isThrowVinegarEnabled = computed(() => false);

// ETA
const etaBias = computed(() => eta(yourRanker.value).toPoints(biasCost.value));
const etaMulti = computed(() =>
  yourRanker.value.rank === 1
    ? Infinity
    : eta(yourRanker.value).toPower(multiCost.value)
);

// Functions

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
 * @typedef {import("../entities/ranker.js").default} Ranker
 */

/**
 * @param {Ranker} ranker
 */

/*
window.eta = eta;
window.secondsToHms = secondsToHms;
window.allRankers = allRankers;
window.rankers = (i) => {
  return allRankers.value[i];
};*/
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
