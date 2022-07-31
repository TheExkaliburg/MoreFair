<template>
  <div class="row py-1">
    <div class="col-6">
      <div class="container px-3">
        <div class="row py-0">
          <div class="col px-0 text-start">
            {{ yourPowerFormatted }}/<span class="text-highlight"
              >{{ multiCostFormatted }}
            </span>
            Power [M: x{{ ("" + yourRanker.multi).padStart(2, "0") }}]
          </div>
          <div class="col px-0 text-end">
            [B: +{{ ("" + yourRanker.bias).padStart(2, "0") }}]
            {{ yourPointsFormatted }}/<span class="text-highlight"
              >{{ biasCostFormatted }}
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
              <span v-else>+M ({{ etaMulti }})</span>
            </button>
            <button
              :class="isBiasEnabled ? '' : 'disabled'"
              class="btn btn-outline-primary shadow-none w-100"
              @click="buyBias"
            >
              <span v-if="isBiasEnabled">+1 Bias</span>
              <span v-else>+B ({{ etaBias }})</span>
            </button>
          </div>
        </div>
        <br />
        <div class="row py-0">
          <div class="col px-0 text-start">
            {{ yourGrapesFormatted }}
            <span v-if="ladder.number >= settings.autoPromoteLadder"
              >/<span class="text-highlight">{{
                numberFormatter.format(autoPromoteCost)
              }}</span></span
            >
            Grapes
            <span
              v-if="
                yourRanker.autoPromote ||
                store.state.ladder.types.has('FREE_AUTO')
              "
              >(Active)</span
            >
          </div>
          <div class="col px-0 text-end">
            {{ yourVinegarFormatted }}/<span class="text-highlight">{{
              numberFormatter.format(vinegarCost)
            }}</span>
            Vinegar
          </div>
        </div>
        <div class="row py-0">
          <div class="col px-0 btn-group d-flex">
            <button
              v-if="ladder.number < settings.assholeLadder"
              :class="[
                isBuyAutoPromoteEnabled ? '' : 'disabled',
                ladder.number >= settings.autoPromoteLadder ? '' : 'hide',
              ]"
              class="btn btn-outline-primary shadow-none w-100"
              @click="buyAutoPromote"
            >
              Buy Autopromote
            </button>
            <button
              v-else-if="ladder.number >= settings.assholeLadder"
              :class="[store.getters['ladder/canPromote'] ? '' : 'disabled']"
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
              {{
                yourRanker.vinegar.cmp(vinegarCost) >= 0
                  ? ""
                  : `(${secondsToHms(
                      (vinegarCost - yourRanker.vinegar) / yourRanker.grapes
                    )})`
              }}
            </button>
            <button
              v-else-if="
                yourRanker.rank === 1 &&
                ladder.number !== settings.assholeLadder
              "
              :class="[store.getters['ladder/canPromote'] ? '' : 'disabled']"
              class="btn btn-outline-primary shadow-none w-100"
              @click="promote"
            >
              Promote
            </button>
          </div>
        </div>
        <div class="row py-0">
          <div class="col px-0 text-start">
            {{
              ladder.number >= settings.assholeLadder ? "Be Asshole" : "Promote"
            }}
            at
            {{ numberFormatter.format(pointsForPromote) }}
            Points ({{
              numberFormatter.format(
                yourRanker.points
                  .mul(100)
                  .div(stats.pointsNeededForManualPromote)
              )
            }}%)
            {{
              yourRanker.points.cmp(stats.pointsNeededForManualPromote) >= 0
                ? ""
                : `(${secondsToHms(etaPromote)})`
            }}
          </div>
          <div class="col px-0 text-end"></div>
        </div>
      </div>
    </div>

    <div class="col-6 tempInfo">
      In case you are lost or need information, look at the
      <router-link to="/help">Help-Page</router-link>
      under the menu at the top right. If that's not enough you can always ask
      away at Chad or join the Discord.
      <br /><br />
      Round Base Point Requirement:
      {{ numberFormatter.format(store.state.settings.pointsForPromote) }}
      <br />Ladder Base Point Requirement:
      {{ numberFormatter.format(store.state.ladder.basePointsToPromote) }}
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, ref } from "vue";
import { eta } from "@/modules/eta";
import { secondsToHms } from "@/modules/formatting";
import Decimal from "break_infinity.js";
import API from "@/websocket/wsApi";

const store = useStore();
const stompClient = inject("$stompClient");

const yourPowerFormatted = computed(() =>
  numberFormatter.value.format(yourRanker.value.power)
);
const yourPointsFormatted = computed(() =>
  numberFormatter.value.format(yourRanker.value.points)
);
const biasCostFormatted = computed(() =>
  numberFormatter.value.format(biasCost.value)
);
const multiCostFormatted = computed(() =>
  numberFormatter.value.format(multiCost.value)
);
const yourGrapesFormatted = computed(() =>
  hideVinegarAndGrapes.value
    ? "[[Hidden]]"
    : numberFormatter.value.format(yourRanker.value.grapes)
);
const yourVinegarFormatted = computed(() =>
  hideVinegarAndGrapes.value
    ? "[[Hidden]]"
    : numberFormatter.value.format(yourRanker.value.vinegar)
);

// Disables a button if its pressed in the last second
const biasLastSecond = ref(false);
const multiLastSecond = ref(false);
const vinegarLastSecond = ref(false);
const buyAutoPromoteLastSecond = ref(false);
const promoteLastSecond = ref(false);

// computed
const ladder = computed(() => store.state.ladder);
const stats = computed(() => store.state.ladder.stats);
const settings = computed(() => store.state.settings);
const numberFormatter = computed(() => store.state.numberFormatter);
const yourRanker = computed(() => store.state.ladder.yourRanker);
const hideVinegarAndGrapes = computed(() =>
  store.getters["options/getOptionValue"]("hideVinAndGrapeCount")
);
const biasCost = computed(() =>
  store.getters["ladder/getNextUpgradeCost"](yourRanker.value.bias)
);
const multiCost = computed(() =>
  store.getters["ladder/getNextUpgradeCost"](yourRanker.value.multi)
);
const canThrowVinegar = computed(() => store.getters["ladder/canThrowVinegar"]);
const vinegarCost = computed(() => store.getters["ladder/getVinegarThrowCost"]);
const autoPromoteCost = computed(
  () => store.getters["ladder/getAutoPromoteCost"]
);
const isBiasEnabled = computed(
  () =>
    yourRanker.value.points.cmp(biasCost.value) >= 0 && !biasLastSecond.value
);
const isMultiEnabled = computed(
  () =>
    yourRanker.value.power.cmp(multiCost.value) >= 0 && !multiLastSecond.value
);

const isBuyAutoPromoteEnabled = computed(
  () =>
    store.getters["ladder/canBuyAutoPromote"] && !buyAutoPromoteLastSecond.value
);

// ETA
const etaBias = computed(() =>
  secondsToHms(eta(yourRanker.value).toPoints(biasCost.value))
);
const etaMulti = computed(() =>
  secondsToHms(
    yourRanker.value.rank === 1
      ? Infinity
      : eta(yourRanker.value).toPower(multiCost.value)
  )
);
const etaPromote = computed(() => {
  if (pointsForPromoteIsInfinity.value) return new Decimal(Infinity);
  return eta(yourRanker.value).toPromote();
});
const pointsForPromote = computed(() => {
  if (pointsForPromoteIsInfinity.value) return new Decimal(Infinity);
  return stats.value.pointsNeededForManualPromote;
});
const pointsForPromoteIsInfinity = computed(
  () =>
    ladder.value.rankers.length <
    Math.max(settings.value.minimumPeopleForPromote, ladder.value.number)
);

// Functions

function throwVinegar(event) {
  if (vinegarLastSecond.value) return;
  vinegarLastSecond.value = true;
  setTimeout(() => (vinegarLastSecond.value = false), 1000);
  stompClient.send(API.GAME.APP_VINEGAR_DESTINATION, { event: event });
}

function buyAutoPromote(event) {
  if (buyAutoPromoteLastSecond.value) return;
  buyAutoPromoteLastSecond.value = true;
  setTimeout(() => (buyAutoPromoteLastSecond.value = false), 1000);
  stompClient.send(API.GAME.APP_AUTOPROMOTE_DESTINATION, { event: event });
}

function buyBias(event) {
  if (biasLastSecond.value) return;
  biasLastSecond.value = true;
  setTimeout(() => (biasLastSecond.value = false), 1000);
  stompClient.send(API.GAME.APP_BIAS_DESTINATION, { event: event });
}

function buyMulti(event) {
  if (multiLastSecond.value) return;
  multiLastSecond.value = true;
  setTimeout(() => (multiLastSecond.value = false), 1000);
  stompClient.send(API.GAME.APP_MULTI_DESTINATION, { event: event });
}

function promote(event) {
  if (promoteLastSecond.value) return;
  promoteLastSecond.value = true;
  setTimeout(() => (promoteLastSecond.value = false), 1000);
  if (ladder.value.number >= settings.value.assholeLadder) {
    if (confirm("Do you really wanna be an Asshole?!"))
      stompClient.send(API.GAME.APP_PROMOTE_DESTINATION, { event: event });
  } else {
    stompClient.send(API.GAME.APP_PROMOTE_DESTINATION, { event: event });
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
  &.tempInfo {
    font-size: 13px;
  }
}

.hide {
  display: none !important;
}
</style>
