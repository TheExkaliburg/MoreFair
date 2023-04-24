<template>
  <div class="flex flex-col w-full">
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
        >+1 {{ lang("multi") }}
      </FairButton>
      <FairButton
        :disabled="!canBuyBias || isButtonLocked"
        class="w-full rounded-l-none border-l-0"
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
        Grapes
      </p>
      <p>
        {{ yourFormattedVinegar }}/<span class="text-text-dark">{{
          yourVinegarThrowCostFormatted
        }}</span>
        Vinegar
      </p>
    </div>
    <div class="flex flex-row w-full">
      <FairButton
        :disabled="!canBuyAutoPromote || isButtonLocked"
        class="w-full rounded-r-none"
        >{{ lang("autopromote") }}
      </FairButton>
      <FairButton
        v-if="yourRanker.rank === 1"
        :disabled="!canThrowVinegar || isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        >{{ lang("vinegar") }}
      </FairButton>
      <FairButton
        v-else
        :disabled="!canPromote || isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        >{{ lang("promote") }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>
        Promote at
        <span class="text-text-dark">{{ pointsNeededToPromoteFormatted }}</span>
        Points
      </p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import FairButton from "../../components/interactables/FairButton.vue";
import { useLang } from "~/composables/useLang";
import { useOptionsStore } from "~/store/options";
import { useLadderStore } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";
import { useFormatter } from "~/composables/useFormatter";
import { useLadderUtils } from "~/composables/useLadderUtils";

const lang = useLang("components.ladder.buttons");
const optionsStore = useOptionsStore();
const ladderStore = useLadderStore();
const ladderUtils = useLadderUtils();
const isButtonLocked = computed<boolean>(() => {
  return optionsStore.state.ladder.lockButtons.value;
});

const yourRanker = computed<Ranker>(() => {
  return ladderStore.getters.yourRanker ?? new Ranker({});
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
  return useFormatter(ladderUtils.getPointsNeededToPromote.value);
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
    ladderUtils.getPointsNeededToPromote.value.cmp(yourRanker.value.points) <= 0
  );
});
</script>

<style scoped></style>
