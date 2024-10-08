<template>
  <div ref="container" class="overflow-y-auto h-full" @scroll="onScroll">
    <div
      class="grid gap-1 px-1 text-sm md:text-base font-bold text-text-light top-0 bg-background z-1"
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
      <LadderWindowTableRow
        v-for="(ranker, index) in ladder.getters.shownRankers"
        :key="ranker.accountId"
        :container="container"
        :class="{
          'border-button-border border-b-1': isScrolled && index === 0,
        }"
        :index="Number(index)"
        :ranker="ranker"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useLadderStore } from "~/store/ladder";
import LadderWindowTableRow from "~/components/ladder/LadderWindowTableRow.vue";
import { useLang } from "~/composables/useLang";
import { useTableSpaceStyles } from "~/composables/useTableSpace";
import { useOptionsStore } from "~/store/options";

const ladder = useLadderStore();
const optionsStore = useOptionsStore();

const lang = useLang("components.ladder.table");
const isScrolled = ref<boolean>(false);

const tableSpaceClasses = useTableSpaceStyles();
const container = ref<HTMLElement | null>(null);

useStomp().addCallback(
  useStomp().callbacks.onTick,
  "fair_ladder_follow",
  followRanker,
);

function onScroll(e: any) {
  requestAnimationFrame(() => {
    isScrolled.value = e.target?.scrollTop > 0;
  });
}

function followRanker() {}
</script>

<style scoped></style>
