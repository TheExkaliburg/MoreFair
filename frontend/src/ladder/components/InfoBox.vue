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
              : `(${secondsToHms(eta(yourRanker).toFirst())})`
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
import { eta } from "../../modules/eta";
import { secondsToHms } from "../../modules/formatting";

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

window.eta = eta;

window.secondsToHms = secondsToHms;
window.allRankers = allRankers;
window.rankers = (i) => {
  return allRankers.value[i];
};
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
