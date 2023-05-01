<template>
  <div
    v-if="active"
    ref="el"
    :class="{
      'bg-ladder-bg-promoted text-ladder-text-promoted': !ranker.growing,
      'bg-ladder-bg-you text-ladder-text-you': isYou,
      'sticky z-1 top-0': isFirst,
      'bg-ladder-bg': isFirst && !isYou && ranker.growing,
    }"
    :style="tableSpaceStyles.total"
    class="grid gap-1 px-1 text-sm select-none width-full"
  >
    <div
      class="whitespace-nowrap overflow-hidden"
      :style="tableSpaceStyles.rank"
    >
      {{ ranker.rank }} {{ ranker.assholeTag }}
      <sub
        v-if="optionsStore.state.general.showAssholePoints.value"
        :class="{ 'text-text-dark': !isYou }"
        >{{ ranker.assholePoints }}</sub
      >
    </div>
    <div
      class="whitespace-nowrap overflow-hidden"
      :style="tableSpaceStyles.username"
    >
      {{ ranker.username
      }}<sub :class="{ 'text-text-dark': !isYou }">#{{ ranker.accountId }}</sub>
    </div>

    <div
      class="text-right whitespace-nowrap overflow-hidden sm:order-last"
      :style="tableSpaceStyles.power"
    >
      {{ formattedPower }}
    </div>
    <div
      class="text-right whitespace-nowrap overflow-hidden sm:order-last etaProgressAnimation"
      :style="[
        'animation-delay: ' + etaPercentage + 's !important',
        tableSpaceStyles.points,
      ]"
    >
      {{ formattedPoints }}
    </div>
    <div
      v-if="tableSpace.etaToLadder > 0"
      class="text-right whitespace-nowrap overflow-hidden etaProgressAnimation"
      :style="[
        'animation-delay: ' + etaPercentage + 's !important',
        tableSpaceStyles.etaToLadder,
      ]"
    >
      {{ etaToNextLadderFormatted }}
    </div>
    <div
      v-if="tableSpace.etaToYou > 0"
      class="text-right whitespace-nowrap overflow-hidden etaProgressAnimation"
      :style="[
        'animation-delay: ' + etaPercentage + 's !important',
        tableSpaceStyles.etaToYou,
      ]"
    >
      {{ etaToYourRankerFormatted }}
    </div>
    <div
      v-if="tableSpace.powerGain > 0"
      class="text-right whitespace-nowrap overflow-hidden"
      :style="tableSpaceStyles.powerGain"
    >
      <span v-if="optionsStore.state.ladder.showPowerGain.value">{{
        formattedPowerPerSec
      }}</span
      ><span v-if="optionsStore.state.ladder.showBiasAndMulti.value"
        >[<span
          :class="{
            'text-eta-best': canBuyBias,
            'text-eta-worst': !canBuyBias,
          }"
          >+{{ formattedBias }}</span
        ><span
          :class="{
            'text-eta-best': canBuyMulti,
            'text-eta-worst': !canBuyMulti,
          }"
        >
          x{{ formattedMulti }}</span
        >]</span
      >
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { MaybeElement, useElementVisibility } from "@vueuse/core";
import { Ranker } from "~/store/entities/ranker";
import { useFormatter, useTimeFormatter } from "~/composables/useFormatter";
import { useEta } from "~/composables/useEta";
import { useLadderStore } from "~/store/ladder";
import { EtaColorType, useOptionsStore } from "~/store/options";
import { useLadderUtils } from "~/composables/useLadderUtils";
import {
  useTableSpace,
  useTableSpaceStyles,
} from "~/composables/useTableSpace";

const props = defineProps({
  ranker: { type: Ranker, required: true },
  active: { type: Boolean, required: false, default: true },
  index: { type: Number, required: false, default: -1 },
});

const ladderStore = useLadderStore();
const optionsStore = useOptionsStore();
const ladderUtils = useLadderUtils();

const el = ref<MaybeElement>();
const isVisible = useElementVisibility(el.value);

const isYou = computed(
  () => props.ranker.accountId === ladderStore.getters.yourRanker?.accountId
);
const isFirst = computed(() => {
  return props.index === 0;
});

const tableSpaceStyles = useTableSpaceStyles();
const tableSpace = useTableSpace();

const formattedPowerPerSec = computed<string>(() => {
  if (!isVisible) return "";
  if (!props.ranker.growing) return "";
  return `(+${useFormatter(props.ranker.getPowerPerSecond())}/s) `;
});

const formattedBias = computed<string>(() => {
  if (!isVisible) return "";
  return props.ranker.bias.toString().padStart(2, "0");
});

const formattedMulti = computed<string>(() => {
  if (!isVisible) return "";
  return props.ranker.multi.toString().padStart(2, "0");
});

const formattedPower = computed<string>(() => {
  if (!isVisible) return "";
  return useFormatter(props.ranker.power);
});
const formattedPoints = computed<string>(() => {
  if (!isVisible) return "";
  return useFormatter(props.ranker.points);
});

const etaToNextLadderFormatted = computed<string>(() => {
  if (!isVisible) return "";
  return useTimeFormatter(etaToNextLadder.value);
});

const etaToNextLadder = computed<number>(() => {
  return useEta(props.ranker).toPromote();
});

const etaToYourRankerFormatted = computed<string>(() => {
  if (!isVisible) return "";
  return useTimeFormatter(etaToYourRanker.value);
});

const etaToYourRanker = computed<number>(() => {
  if (ladderStore.getters.yourRanker === undefined) return Infinity;
  return useEta(props.ranker).toRanker(ladderStore.getters.yourRanker);
});

const canBuyBias = computed<boolean>(() => {
  if (!isVisible) return false;
  const upgradeCost = ladderUtils.getNextUpgradeCost(props.ranker.bias);
  return upgradeCost.cmp(props.ranker.points) <= 0;
});

const canBuyMulti = computed<boolean>(() => {
  if (!isVisible) return false;
  const upgradeCost = ladderUtils.getNextUpgradeCost(props.ranker.multi);
  return upgradeCost.cmp(props.ranker.power) <= 0;
});

// should return the value from fastest (0%) to as long as it takes for the top (50%) to double as long (100%)
// as a negative, because the animation-delay only sets the start value if the delay is negative, otherwise it's an actual delay
const etaPercentage = computed<number>(() => {
  if (optionsStore.state.ladder.etaColors.value === EtaColorType.OFF) return 1;
  if (!props.ranker.growing) return 1;
  if (isYou.value) return 1;
  const yourRanker = ladderStore.getters.yourRanker;
  if (yourRanker === undefined) return -100;

  const etaRankerToPromote = etaToNextLadder.value;
  const etaYouToPromote = useEta(yourRanker).toPromote();

  // we want to return a percentage for our animation interpolation
  // 0 is to overtake now
  // 50 is eta to overtake equals eta to first
  // 100 is eta to overtake equals eta to first * 2
  let gradientPercent = (etaYouToPromote / etaRankerToPromote) * 50;
  gradientPercent = Math.min(Math.max(gradientPercent, 0), 100);

  // check if the ranker is behind us
  if (props.ranker.rank > yourRanker.rank) {
    // we want to return a percentage for our animation interpolation
    // 0 is eta to overtake equals eta to first * 2
    // 50 is eta to overtake equals eta to first
    // 100 is 0 seconds to overtake
    // gradientPercent = 100 - gradientPercent;
  }

  if (optionsStore.state.ladder.etaColors.value === EtaColorType.COLORS) {
    if (gradientPercent < 45) {
      gradientPercent = 0;
    } else if (gradientPercent < 55) {
      gradientPercent = 50;
    } else {
      gradientPercent = 100;
    }
  }

  return -gradientPercent;
});
</script>

<style scoped lang="scss">
@keyframes etaProgress {
  0% {
    color: var(--eta-best-color);
  }
  50% {
    color: var(--eta-mid-color);
  }
  100% {
    color: var(--eta-worst-color);
  }
}

.etaProgressAnimation {
  // The Animation moves through the keyframes but is paused,
  // so only the negative delay can change anything for it
  animation: 101s ease-in-out paused etaProgress;
}
</style>