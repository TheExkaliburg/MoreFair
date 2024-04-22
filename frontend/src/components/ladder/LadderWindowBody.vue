<template>
  <div class="flex flex-col w-full 2xl:w-1/2 2xl:self-end relative">
    <LockClosedIcon
      v-if="optionsStore.state.ladder.lockButtons.value"
      class="absolute w-24 top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-10 text-text-light opacity-50"
    />
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <div class="whitespace-nowrap">
        {{ yourFormattedPower }}/<span class="text-text-dark">{{
          yourMultiCostFormatted
        }}</span>
        {{ lang("info.power") }} [{{ lang("info.multi_short") }}: x{{
          yourFormattedMulti
        }}]
      </div>
      <div class="whitespace-nowrap">
        [{{ lang("info.bias_short") }}: +{{ yourFormattedBias }}]
        {{ yourFormattedPoints }}/<span class="text-text-dark">{{
          yourBiasCostFormatted
        }}</span>
        {{ lang("info.points") }}
      </div>
    </div>
    <div class="flex flex-row">
      <FairButton
        :class="{ 'border-r-0': isMultiButtonDisabled }"
        :disabled="isMultiButtonDisabled"
        class="w-full rounded-r-none whitespace-nowrap"
        data-tutorial="multi"
        @click="buyMulti"
      >
        {{ multiButtonLabel }}
      </FairButton>
      <FairButton
        :class="{ 'border-l-1': isMultiButtonDisabled }"
        :disabled="isBiasButtonDisabled"
        class="w-full rounded-l-none border-l-0 whitespace-nowrap"
        data-tutorial="bias"
        @click="buyBias"
      >
        {{ biasButtonLabel }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p data-tutorial="grapes">
        {{ yourFormattedGrapes }}/<span class="text-text-dark">{{
          yourAutoPromoteCostFormatted
        }}</span>
        {{ lang("info.grapes") }}
        <span
          v-if="
            yourRanker.autoPromote ||
            ladderStore.state.types.has(LadderType.FREE_AUTO)
          "
          >({{ lang("info.active") }})</span
        >
      </p>
      <p data-tutorial="vinegar">
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
        class="w-full rounded-r-none whitespace-nowrap"
        @click="buyAutoPromote"
      >
        {{ lang("autopromote") }}
      </FairButton>
      <FairButton
        v-if="yourRanker.rank !== 1"
        :class="{ 'border-l-1': isAutoPromoteButtonDisabled }"
        :disabled="isVinegarButtonDisabled"
        class="w-full rounded-l-none border-l-0 whitespace-nowrap"
        @click="throwVinegar"
      >
        {{ vinegarButtonLabel }}
      </FairButton>
      <FairButton
        v-else
        :class="{ 'border-l-1': isAutoPromoteButtonDisabled }"
        :disabled="isPromoteButtonDisabled"
        class="w-full rounded-l-none border-l-0 whitespace-nowrap"
        @click="promote"
      >
        {{ promoteLabel }}
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
import { LockClosedIcon } from "@heroicons/vue/24/outline";
import FairButton from "../../components/interactables/FairButton.vue";
import { useLang } from "~/composables/useLang";
import { useOptionsStore } from "~/store/options";
import { LadderType, useLadderStore } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";
import { useFormatter, useTimeFormatter } from "~/composables/useFormatter";
import { useLadderUtils } from "~/composables/useLadderUtils";
import { useStomp } from "~/composables/useStomp";
import { useEta } from "~/composables/useEta";
import { useGrapesStore } from "~/store/grapes";

const lang = useLang("components.ladder.buttons");
const optionsStore = useOptionsStore();
const ladderStore = useLadderStore();
const ladderUtils = useLadderUtils();
const isButtonLocked = computed<boolean>(() => {
  return (
    optionsStore.state.ladder.lockButtons.value || !yourRanker.value.growing
  );
});

const yourRanker = computed<Ranker>(() => {
  return (
    ladderStore.getters.yourRanker ?? new Ranker({ rank: 0, growing: false })
  );
});
const yourTimeToPromotion = computed<string>(() => {
  return useTimeFormatter(useEta(yourRanker.value).toPromote());
});

const yourPercentageToPromotion = computed<string>(() => {
  return useFormatter(
    yourRanker.value.points
      .mul(100)
      .div(ladderUtils.getYourPointsNeededToPromote.value),
  );
});

const biasButtonLabel = computed<string>(() => {
  if (canBuyBias.value) return `+1 ${lang("bias")}`;

  const eta = useEta(yourRanker.value).toPoints(
    ladderUtils.getYourBiasCost.value,
  );
  return `+${lang("bias_short")} (${useTimeFormatter(eta)})`;
});

const multiButtonLabel = computed<string>(() => {
  if (canBuyMulti.value) return `+1 ${lang("multi")}`;

  const eta = useEta(yourRanker.value).toPower(
    ladderUtils.getYourMultiCost.value,
  );
  return `+${lang("multi_short")} (${useTimeFormatter(eta)})`;
});

const vinegarButtonLabel = computed<string>(() => {
  const eta = useEta(yourRanker.value).toVinegarThrow();
  if (eta === 0 || eta === Infinity) {
    return `${lang(
      "vinegar",
      useFormatter(useGrapesStore().getters.selectedVinegar),
    )}`;
  }
  return `${lang("vinegar")} (${useTimeFormatter(eta)})`;
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
  if (optionsStore.state.ladder.hideVinegarAndGrapes.value) return "[Hidden]";
  return useFormatter(yourRanker.value.grapes);
});
const yourFormattedVinegar = computed<string>(() => {
  if (optionsStore.state.ladder.hideVinegarAndGrapes.value) return "[Hidden]";
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
      0 &&
    !yourRanker.value.autoPromote &&
    !ladderStore.state.types.has(LadderType.FREE_AUTO) &&
    !ladderStore.state.types.has(LadderType.NO_AUTO)
  );
});
const canThrowVinegar = computed<boolean>(() => {
  return (
    ladderUtils.getVinegarThrowCost.value.cmp(
      useGrapesStore().getters.selectedVinegar,
    ) <= 0 &&
    ladderStore.state.rankers[0].growing &&
    ladderUtils.isLadderPromotable.value
  );
});
const canPromote = computed<boolean>(() => {
  return (
    ladderUtils.getYourPointsNeededToPromote.value.cmp(
      yourRanker.value.points,
    ) <= 0 &&
    yourRanker.value.growing &&
    ladderUtils.isLadderPromotable.value
  );
});
const promoteLabel = computed<string>(() => {
  if (ladderStore.state.types.has(LadderType.END)) return lang("wait");
  if (ladderStore.state.types.has(LadderType.ASSHOLE)) return lang("asshole");
  return lang("promote");
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
  useStomp().wsApi.ladder.throwVinegar(
    e,
    useGrapesStore().state.vinegarThrowPercentage,
  );
}

function promote(e: Event) {
  if (pressedPromoteRecently.value || !canPromote.value) return;
  pressedPromoteRecently.value = true;
  if (ladderStore.state.types.has(LadderType.ASSHOLE)) {
    if (confirm("Do you really wanna be an Asshole?!")) {
      useStomp().wsApi.ladder.promote(e);
    }
  } else {
    useStomp().wsApi.ladder.promote(e);
  }
}

onMounted(() => {
  useStomp().addCallback(
    useStomp().callbacks.onTick,
    "fair_ladder_buttons",
    handleTick,
  );
});
</script>

<style scoped></style>
