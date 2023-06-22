<template>
  <div class="overflow-y-auto h-full" @scroll="onScroll">
    <!--DynamicScroller
      :buffer="200"
      :items="ladder.rankers"
      :min-item-size="23"
      class="h-full overflow-y-auto text-sm"
      key-field="rank"
      page-mode
    >
      <template #before>
        <div
          class="grid grid-cols-24 gap-1 px-1 px-1 font-bold text-text-light sticky top-0"
        >
          <div class="col-span-3">#</div>
          <div class="col-span-9">Username</div>
          <div class="col-span-6 text-right">Power</div>
          <div class="col-span-6 text-right">Points</div>
        </div>
      </template>
      <template #default="{ item, index, active }">
        <DynamicScrollerItem
          :active="active"
          :data-active="active"
          :data-index="index"
          :item="item"
          :size-dependencies="[item]"
        >
          <LadderWindowTableRow
            :active="active"
            :index="index"
            :ranker="item"
          />
        </DynamicScrollerItem>
      </template>
    </DynamicScroller-->
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

useStomp().addCallback(
  useStomp().callbacks.onTick,
  "fair_ladder_follow",
  followRanker
);

function onScroll(e: any) {
  isScrolled.value = e.target?.scrollTop > 0;
}

function followRanker() {}
</script>

<style scoped></style>
