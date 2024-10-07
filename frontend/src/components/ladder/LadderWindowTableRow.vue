<template>
  <div
    ref="el"
    :class="{
      'bg-ladder-bg-promoted text-ladder-text-promoted': !ranker.growing,
      'bg-ladder-bg-you text-ladder-text-you ranker-you': isYou,
      'sticky z-1 top-0 ranker-1': isFirst,
      'bg-ladder-bg': isFirst && !isYou && ranker.growing,
    }"
    :style="tableSpaceStyles.total"
    class="grid gap-1 px-1 text-sm select-none width-full"
  >
    <div
      :style="tableSpaceStyles.rank"
      class="whitespace-nowrap overflow-hidden"
    >
      {{ ranker.rank }} {{ ranker.assholeTag }}
      <sub
        v-if="optionsStore.state.general.showAssholePoints.value"
        :class="{ 'text-text-dark': !isYou }"
        >{{ ranker.assholePoints }}</sub
      >
    </div>
    <div
      :style="tableSpaceStyles.username"
      class="whitespace-nowrap overflow-hidden"
    >
      {{ ranker.username
      }}<sub :class="{ 'text-text-dark': !isYou }">#{{ ranker.accountId }}</sub>
    </div>

    <div
      :style="tableSpaceStyles.power"
      class="text-right whitespace-nowrap overflow-hidden sm:order-last power"
    >
      {{ formattedPower }}
    </div>
    <div
      :style="[
        'animation-delay: ' + etaPercentage + 's !important',
        tableSpaceStyles.points,
      ]"
      class="text-right whitespace-nowrap overflow-hidden sm:order-last etaProgressAnimation points"
    >
      {{ formattedPoints }}
    </div>
    <div
      v-if="tableSpace.etaToLadder > 0"
      :style="[
        'animation-delay: ' + etaPercentage + 's !important',
        tableSpaceStyles.etaToLadder,
      ]"
      class="text-right whitespace-nowrap overflow-hidden etaProgressAnimation"
    >
      {{ etaToNextLadderFormatted }}
    </div>
    <div
      v-if="tableSpace.etaToYou > 0"
      :style="[
        'animation-delay: ' + etaPercentage + 's !important',
        tableSpaceStyles.etaToYou,
      ]"
      class="text-right whitespace-nowrap overflow-hidden etaProgressAnimation"
    >
      {{ etaToYourRankerFormatted }}
    </div>
    <div
      v-if="tableSpace.powerGain > 0"
      :style="tableSpaceStyles.powerGain"
      class="text-right whitespace-nowrap overflow-hidden"
    >
      <span v-if="optionsStore.state.ladder.showPowerGain.value">{{
        formattedPowerPerSec
      }}</span
      ><span v-if="optionsStore.state.ladder.showBiasAndMulti.value"
        >[<span
          :class="{
            'text-eta-best': canBuyBias && showMultiBiasColor,
            'text-eta-mid':
              !canBuyBias && canAlmostBuyBias && showMultiBiasColor,
            'text-eta-worst':
              !canBuyBias && !canAlmostBuyBias && showMultiBiasColor,
          }"
          >+{{ formattedBias }}</span
        ><span
          :class="{
            'text-eta-best': canBuyMulti && showMultiBiasColor,
            'text-eta-mid':
              !canBuyMulti && canAlmostBuyMulti && showMultiBiasColor,
            'text-eta-worst':
              !canBuyMulti && !canAlmostBuyMulti && showMultiBiasColor,
          }"
        >
          x{{ formattedMulti }}</span
        >]</span
      >
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useElementVisibility } from "@vueuse/core";
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
import { useRoundStore } from "~/store/round";

const props = withDefaults(
  defineProps<{
    ranker: Ranker;
    index: number;
    container?: HTMLElement | null;
  }>(),
  {
    index: -1,
    container: null,
  },
);

const roundStore = useRoundStore();
const ladderStore = useLadderStore();
const optionsStore = useOptionsStore();
const ladderUtils = useLadderUtils();

const el = ref<HTMLElement | null>();
const isVisible = useElementVisibility(el.value);

const isYou = computed(
  () => props.ranker.accountId === ladderStore.getters.yourRanker?.accountId,
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

  let currentUpgrade = props.ranker.bias;
  if (roundStore.state.number === 300) {
    const ranksBehind =
      roundStore.state.highestAssholeCount - props.ranker.assholeCount;
    currentUpgrade -= ranksBehind;
  }

  const upgradeCost = ladderUtils.getNextUpgradeCost(currentUpgrade);
  return upgradeCost.cmp(props.ranker.points) <= 0;
});

const canAlmostBuyBias = computed<boolean>(() => {
  if (!isVisible) return false;

  let currentUpgrade = props.ranker.bias;
  if (roundStore.state.number === 300) {
    const ranksBehind =
      roundStore.state.highestAssholeCount - props.ranker.assholeCount;
    currentUpgrade -= ranksBehind;
  }

  const upgradeCost = ladderUtils.getNextUpgradeCost(currentUpgrade);
  return useEta(props.ranker).toPoints(upgradeCost) < 5 * 60;
});

const canBuyMulti = computed<boolean>(() => {
  if (!isVisible) return false;

  let currentUpgrade = props.ranker.multi;
  if (roundStore.state.number === 300) {
    const ranksBehind =
      roundStore.state.highestAssholeCount - props.ranker.assholeCount;
    currentUpgrade -= ranksBehind;
  }

  const upgradeCost = ladderUtils.getNextUpgradeCost(currentUpgrade);
  return upgradeCost.cmp(props.ranker.power) <= 0;
});

const canAlmostBuyMulti = computed<boolean>(() => {
  if (!isVisible) return false;

  let currentUpgrade = props.ranker.multi;
  if (roundStore.state.number === 300) {
    const ranksBehind =
      roundStore.state.highestAssholeCount - props.ranker.assholeCount;
    currentUpgrade -= ranksBehind;
  }

  const upgradeCost = ladderUtils.getNextUpgradeCost(currentUpgrade);
  return useEta(props.ranker).toPower(upgradeCost) < 5 * 60;
});

const showMultiBiasColor = computed<boolean>(() => {
  return optionsStore.state.ladder.biasMultiColors.value;
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

onMounted(() => {
  if (
    isYou.value &&
    optionsStore.state.ladder.followOwnRanker.value &&
    el.value &&
    props.container
  ) {
    requestAnimationFrame(() => {
      scrollToCenter(el.value, props.container);
    });
  }
});

onUpdated(() => {
  if (
    isYou.value &&
    optionsStore.state.ladder.followOwnRanker.value &&
    el.value &&
    props.container
  ) {
    requestAnimationFrame(() => {
      scrollToCenter(el.value, props.container);
    });
  }
});
</script>

<style lang="scss" scoped>
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
