<template>
  <div
    ref="container"
    class="h-full"
    :class="isFollowing ? 'overflow-y-hidden' : 'overflow-y-auto'"
    style="overflow-anchor: none"
    @scroll="onScroll"
  >
    <div
      ref="header"
      class="grid gap-1 px-1 text-sm md:text-base font-bold text-text-light top-0 bg-background z-[1]"
      :style="tableSpaceClasses.total"
    >
      <div class="whitespace-nowrap" :style="tableSpaceClasses.rank">#</div>
      <div class="whitespace-nowrap" :style="tableSpaceClasses.username">
        {{ lang("username") }}
      </div>
      <div
        class="whitespace-nowrap text-right sm:order-last"
        :style="tableSpaceClasses.power"
      >
        {{ lang("power") }}
      </div>
      <div
        class="whitespace-nowrap text-right sm:order-last"
        :style="tableSpaceClasses.points"
      >
        {{ lang("points") }}
      </div>
      <div
        v-if="optionsStore.state.ladder.showEta.value"
        class="whitespace-nowrap text-right"
        :style="tableSpaceClasses.etaToLadder"
      >
        {{ lang("eta") }} -> {{ lang("ladder_short")
        }}{{ ladder.state.number + 1 }}
      </div>
      <div
        v-if="optionsStore.state.ladder.showEta.value"
        class="whitespace-nowrap text-right"
        :style="tableSpaceClasses.etaToYou"
      >
        {{ lang("eta") }} -> {{ lang("you") }}
      </div>
      <div
        v-if="
          optionsStore.state.ladder.showBiasAndMulti.value ||
          optionsStore.state.ladder.showPowerGain.value
        "
        class="whitespace-nowrap text-right"
        :style="tableSpaceClasses.powerGain"
      >
        {{ lang("powerGain") }}
      </div>
    </div>
    <div>
      <div
        v-if="firstRanker"
        ref="firstRow"
        class="sticky z-[1] top-0"
        :class="{ 'border-button-border border-b': isScrolled }"
      >
        <LadderWindowTableRow
          :key="firstRanker.accountId"
          :container="container"
          :index="0"
          :ranker="firstRanker"
        />
      </div>
      <div :style="{ height: `${(startIndex - 1) * rowHeight}px` }" />
      <LadderWindowTableRow
        v-for="(ranker, index) in visibleRankers"
        :key="ranker.accountId"
        :container="container"
        :index="startIndex + index"
        :ranker="ranker"
      />
      <div
        :style="{
          height: `${Math.max(0, shownRankers.length - endIndex) * rowHeight}px`,
        }"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useElementSize } from "@vueuse/core";
import { useLadderStore } from "~/store/ladder";
import LadderWindowTableRow from "~/components/ladder/LadderWindowTableRow.vue";
import { useLang } from "~/composables/useLang";
import { useTableSpaceStyles } from "~/composables/useTableSpace";
import { useOptionsStore } from "~/store/options";

const ladder = useLadderStore();
const optionsStore = useOptionsStore();

const lang = useLang("components.ladder.table");
const tableSpaceClasses = useTableSpaceStyles();
const container = ref<HTMLElement | null>(null);
const header = ref<HTMLElement | null>(null);
const firstRow = ref<HTMLElement | null>(null);
const scrollTop = ref(0);
const isScrolled = computed(() => scrollTop.value > 0);
const { height: viewportHeight } = useElementSize(container);
const { height: headerHeight } = useElementSize(header);
const { height: measuredRowHeight } = useElementSize(firstRow);
const rowHeight = computed(() => measuredRowHeight.value || 20);
const shownRankers = computed(() => ladder.getters.shownRankers);
const firstRanker = computed(() => shownRankers.value[0]);
const followedRankerIndex = computed(() => {
  if (!optionsStore.state.ladder.followOwnRanker.value) return -1;
  const accountId = ladder.getters.yourRanker?.accountId;
  if (accountId === undefined) return -1;
  return shownRankers.value.findIndex(
    (ranker) => ranker.accountId === accountId,
  );
});
const isFollowing = computed(() => followedRankerIndex.value >= 0);
const buffer = 10;

// Keep the first row mounted for sticky positioning and responsive row sizing.
const startIndex = computed(() =>
  Math.max(
    1,
    Math.min(
      shownRankers.value.length - 1,
      Math.floor((scrollTop.value - headerHeight.value) / rowHeight.value) -
        buffer,
    ),
  ),
);
const endIndex = computed(() =>
  Math.min(
    shownRankers.value.length,
    Math.max(
      startIndex.value,
      Math.ceil(
        (scrollTop.value - headerHeight.value + viewportHeight.value) /
          rowHeight.value,
      ) + buffer,
    ),
  ),
);
const visibleRankers = computed(() =>
  shownRankers.value.slice(startIndex.value, endIndex.value),
);

function onScroll(event: Event) {
  if (event.currentTarget instanceof HTMLElement) {
    scrollTop.value = event.currentTarget.scrollTop;
  }
}

function followRanker() {
  if (!container.value || !isFollowing.value) return;
  const index = followedRankerIndex.value;

  const top =
    index === 0
      ? 0
      : Math.max(
          0,
          headerHeight.value +
            (index + 0.5) * rowHeight.value -
            viewportHeight.value / 2,
        );
  container.value.scrollTop = top;
  scrollTop.value = container.value.scrollTop;
}

watch(
  [shownRankers, followedRankerIndex, rowHeight, headerHeight, viewportHeight],
  followRanker,
  { flush: "post" },
);
onMounted(followRanker);
</script>

<style scoped></style>
