<template>
  <div class="flex flex-col w-full 2xl:w-1/2 2xl:self-end">
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <div>
        {{ yourFormattedPoints }}/<span class="text-text-dark">{{
          yourBiasCostFormatted
        }}</span>
        {{ lang("info.power") }} [{{ lang("info.multi_short") }}: x{{
          yourFormattedMulti
        }}]
      </div>
      <div>
        [{{ lang("info.bias_short") }}: +{{ yourFormattedBias }}]
        {{ yourFormattedPower }}/<span class="text-text-dark">{{
          yourMultiCostFormatted
        }}</span>
        {{ lang("info.points") }}
      </div>
    </div>
    <div class="flex flex-row">
      <FairButton
        :disabled="isMultiButtonDisabled"
        :class="{ 'border-r-0': isMultiButtonDisabled }"
        class="w-full rounded-r-none"
        @click="buyMulti"
        >+1 {{ lang("multi") }}
      </FairButton>
      <FairButton
        :disabled="isBiasButtonDisabled"
        :class="{ 'border-l-1': isMultiButtonDisabled }"
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
        <span v-if="yourRanker.autoPromote">({{ lang("info.active") }})</span>
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
        :class="{ 'border-r-0': isAutoPromoteButtonDisabled }"
        :disabled="isAutoPromoteButtonDisabled"
        class="w-full rounded-r-none"
        @click="buyAutoPromote"
        >{{ lang("autopromote") }}x
      </FairButton>
      <FairButton
        v-if="yourRanker.rank !== 1"
        :class="{ 'border-l-1': isAutoPromoteButtonDisabled }"
        :disabled="isVinegarButtonDisabled"
        class="w-full rounded-l-none border-l-0"
        @click="throwVinegar"
        >{{ lang("vinegar") }}
      </FairButton>
      <FairButton
        v-else
        :class="{ 'border-l-1': isAutoPromoteButtonDisabled }"
        :disabled="isPromoteButtonDisabled"
        class="w-full rounded-l-none border-l-0"
        @click="promote"
        >{{ promoteLabel }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>
        {{ lang("info.promoteAt") }}
        <span class="text-text-dark">{{ pointsNeededToPromoteFormatted }}</span>
        {{ lang("info.points") }} ({{ yourPercentageToPromotion }}%) ({{
          yourTimeToPromotion
        }})
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
import { useEta } from "~/composables/useEta";

const lang = useLang("components.ladder.buttons");
const optionsStore = useOptionsStore();
const ladderStore = useLadderStore();
const roundStore = useRoundStore();
const ladderUtils = useLadderUtils();
const isButtonLocked = computed<boolean>(() => {
  return optionsStore.state.ladder.lockButtons.value;
});

const yourRanker = computed<Ranker>(() => {
  return ladderStore.getters.yourRanker ?? new Ranker({ rank: 0 });
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
    ladderUtils.getYourAutoPromoteCost.value.cmp(yourRanker.value.grapes) <=
      0 && !yourRanker.value.autoPromote
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

const isBiasButtonDisabled = computed<boolean>(() => {
  return !canBuyBias.value || isButtonLocked.value || pressedBiasRecently.value;
});
const isMultiButtonDisabled = computed<boolean>(() => {
  return (
    !canBuyMulti.value || isButtonLocked.value || pressedMultiRecently.value
  );
});
const isVinegarButtonDisabled = computed<boolean>(() => {
  return (
    !canThrowVinegar.value ||
    isButtonLocked.value ||
    pressedVinegarRecently.value
  );
});
const isAutoPromoteButtonDisabled = computed<boolean>(() => {
  return (
    !canBuyAutoPromote.value ||
    isButtonLocked.value ||
    pressedAutoPromoteRecently.value
  );
});
const isPromoteButtonDisabled = computed<boolean>(() => {
  return (
    !canPromote.value || isButtonLocked.value || pressedPromoteRecently.value
  );
});

function handleTick() {
  pressedBiasRecently.value = false;
  pressedMultiRecently.value = false;
  pressedVinegarRecently.value = false;
  pressedAutoPromoteRecently.value = false;
  pressedPromoteRecently.value = false;
}

function buyBias(e: Event) {
  if (pressedBiasRecently.value || !canBuyBias.value) return;
  pressedBiasRecently.value = true;
  useStomp().wsApi.ladder.buyBias(e);
}

function buyMulti(e: Event) {
  if (pressedMultiRecently.value || !canBuyMulti.value) return;
  pressedMultiRecently.value = true;
  useStomp().wsApi.ladder.buyMulti(e);
}

function buyAutoPromote(e: Event) {
  if (pressedAutoPromoteRecently.value || !canBuyAutoPromote.value) return;
  pressedAutoPromoteRecently.value = true;
  useStomp().wsApi.ladder.buyAutoPromote(e);
}

function throwVinegar(e: Event) {
  if (pressedVinegarRecently.value || !canThrowVinegar.value) return;
  pressedVinegarRecently.value = true;
  useStomp().wsApi.ladder.throwVinegar(e);
}

function promote(e: Event) {
  if (pressedPromoteRecently.value || !canPromote.value) return;
  pressedPromoteRecently.value = true;
  useStomp().wsApi.ladder.promote(e);
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
