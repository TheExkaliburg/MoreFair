<template>
  <div class="flex flex-col w-full 2xl:w-1/2 2xl:self-end">
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <div>
        {{ yourFormattedPoints }}/<span class="text-text-dark">{{
          yourBiasCostFormatted
        }}</span>
        Power [M: x{{ yourFormattedMulti }}]
      </div>
      <div>
        [B: +{{ yourFormattedBias }}] {{ yourFormattedPower }}/<span
          class="text-text-dark"
          >{{ yourMultiCostFormatted }}</span
        >
        Points
      </div>
    </div>
    <div class="flex flex-row">
      <FairButton
        :disabled="!canBuyMulti || isButtonLocked"
        class="w-full rounded-r-none"
        @click="buyMulti"
        >+1 {{ lang("multi") }}
      </FairButton>
      <FairButton
        :disabled="!canBuyBias || isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        @click="buyBias"
        >+1 {{ lang("bias") }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>
        {{ yourFormattedGrapes }}/<span class="text-text-dark">{{
          yourAutoPromoteCostFormatted
        }}</span>
        {{ lang("info.grapes") }}
        <span v-if="yourRanker.autoPromote">( {{ lang("info.active") }})</span>
      </p>
      <p>
        {{ yourFormattedVinegar }}/<span class="text-text-dark">{{
          yourVinegarThrowCostFormatted
        }}</span>
        {{ lang("info.vinegar") }}
      </p>
    </div>
    <div class="flex flex-row w-full">
      <FairButton
        :disabled="!canBuyAutoPromote || isButtonLocked"
        class="w-full rounded-r-none"
        @click="buyAutoPromote"
        >{{ lang("autopromote") }}
      </FairButton>
      <FairButton
        v-if="yourRanker.rank !== 1"
        :disabled="!canThrowVinegar || isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        @click="throwVinegar"
        >{{ lang("vinegar") }}
      </FairButton>
      <FairButton
        v-else
        :disabled="!canPromote || isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        @click="promote"
        >{{ promoteLabel }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>
        Promote at
        <span class="text-text-dark">{{ pointsNeededToPromoteFormatted }}</span>
        Points ({{ yourPercentageToPromotion }}%) ({{ yourTimeToPromotion }})
      </p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import FairButton from "../../components/interactables/FairButton.vue";
import { useLang } from "~/composables/useLang";
import { useOptionsStore } from "~/store/options";
import { useLadderStore } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";
import { useFormatter, useTimeFormatter } from "~/composables/useFormatter";
import { useLadderUtils } from "~/composables/useLadderUtils";
import { useRoundStore } from "~/store/round";
import { useStomp } from "~/composables/useStomp";

const lang = useLang("components.ladder.buttons");
const optionsStore = useOptionsStore();
const ladderStore = useLadderStore();
const roundStore = useRoundStore();
const ladderUtils = useLadderUtils();
const isButtonLocked = computed<boolean>(() => {
  return optionsStore.state.ladder.lockButtons.value;
});

const yourRanker = computed<Ranker>(() => {
  return ladderStore.getters.yourRanker ?? new Ranker({ rank: Infinity });
});
const yourTimeToPromotion = computed<string>(() => {
  return useTimeFormatter(useEta(yourRanker.value).toPromote());
});

const yourPercentageToPromotion = computed<string>(() => {
  return useFormatter(
    yourRanker.value.points
      .mul(100)
      .div(ladderUtils.getYourPointsNeededToPromote.value)
  );
});

const yourFormattedMulti = computed<string>(() => {
  return yourRanker.value.multi.toString().padStart(2, "0");
});
const yourFormattedBias = computed<string>(() => {
  return yourRanker.value.bias.toString().padStart(2, "0");
});
const yourFormattedPoints = computed<string>(() => {
  return useFormatter(yourRanker.value.points);
});
const yourFormattedPower = computed<string>(() => {
  return useFormatter(yourRanker.value.power);
});
const yourFormattedGrapes = computed<string>(() => {
  return useFormatter(yourRanker.value.grapes);
});
const yourFormattedVinegar = computed<string>(() => {
  return useFormatter(yourRanker.value.vinegar);
});

const yourBiasCostFormatted = computed<string>(() => {
  return useFormatter(ladderUtils.getYourBiasCost.value);
});
const yourMultiCostFormatted = computed<string>(() => {
  return useFormatter(ladderUtils.getYourMultiCost.value);
});
const yourAutoPromoteCostFormatted = computed<string>(() => {
  return useFormatter(ladderUtils.getYourAutoPromoteCost.value);
});
const yourVinegarThrowCostFormatted = computed<string>(() => {
  return useFormatter(ladderUtils.getVinegarThrowCost.value);
});
const pointsNeededToPromoteFormatted = computed<string>(() => {
  return useFormatter(ladderUtils.getYourPointsNeededToPromote.value);
});

const canBuyBias = computed<boolean>(() => {
  return ladderUtils.getYourBiasCost.value.cmp(yourRanker.value.points) <= 0;
});
const canBuyMulti = computed<boolean>(() => {
  return ladderUtils.getYourMultiCost.value.cmp(yourRanker.value.power) <= 0;
});
const canBuyAutoPromote = computed<boolean>(() => {
  return (
    ladderUtils.getYourAutoPromoteCost.value.cmp(yourRanker.value.grapes) <= 0
  );
});
const canThrowVinegar = computed<boolean>(() => {
  return (
    ladderUtils.getVinegarThrowCost.value.cmp(yourRanker.value.vinegar) <= 0
  );
});
const canPromote = computed<boolean>(() => {
  return (
    ladderUtils.getYourPointsNeededToPromote.value.cmp(
      yourRanker.value.points
    ) <= 0
  );
});
const promoteLabel = computed<string>(() => {
  return ladderStore.state.number < roundStore.state.assholeLadder
    ? lang("promote")
    : lang("asshole");
});

const pressedBiasRecently = ref<boolean>(false);
const pressedMultiRecently = ref<boolean>(false);
const pressedVinegarRecently = ref<boolean>(false);
const pressedAutoPromoteRecently = ref<boolean>(false);
const pressedPromoteRecently = ref<boolean>(false);

function handleTick() {
  pressedBiasRecently.value = false;
  pressedMultiRecently.value = false;
  pressedVinegarRecently.value = false;
  pressedAutoPromoteRecently.value = false;
  pressedPromoteRecently.value = false;
}

function buyBias() {
  if (pressedBiasRecently.value || !canBuyBias.value) return;
  pressedBiasRecently.value = true;
  useStomp().wsApi.ladder.buyBias();
}

function buyMulti() {
  if (pressedMultiRecently.value || !canBuyMulti.value) return;
  pressedMultiRecently.value = true;
  useStomp().wsApi.ladder.buyMulti();
}

function buyAutoPromote() {
  if (pressedAutoPromoteRecently.value || !canBuyAutoPromote.value) return;
  pressedAutoPromoteRecently.value = true;
  useStomp().wsApi.ladder.buyAutoPromote();
}

function throwVinegar() {
  if (pressedVinegarRecently.value || !canThrowVinegar.value) return;
  pressedVinegarRecently.value = true;
  useStomp().wsApi.ladder.throwVinegar();
}

function promote() {
  if (pressedPromoteRecently.value || !canPromote.value) return;
  pressedPromoteRecently.value = true;
  useStomp().wsApi.ladder.promote();
}

onMounted(() => {
  useStomp().addCallback(
    useStomp().callbacks.onTick,
    "fair_ladder_buttons",
    handleTick
  );
});
</script>

<style scoped></style>
