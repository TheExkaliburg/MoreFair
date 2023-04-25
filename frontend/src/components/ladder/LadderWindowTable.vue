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
      class="grid grid-cols-24 sm:grid-cols-48 gap-1 px-1 font-bold text-text-light top-0 bg-background z-1"
    >
      <div class="col-span-3">#</div>
      <div class="col-span-9">{{ lang("username") }}</div>
      <div class="col-span-6 text-right order-last">{{ lang("power") }}</div>
      <div class="col-span-6 text-right order-last">{{ lang("points") }}</div>
      <div class="hidden sm:block col-span-7 text-right">
        {{ lang("eta") }} -> L{{ ladder.state.number + 1 }}
      </div>
      <div class="hidden sm:block col-span-7 text-right">
        {{ lang("eta") }} -> {{ lang("you") }}
      </div>
      <div class="hidden sm:block col-span-10 text-right">
        {{ lang("powerGain") }}
      </div>
    </div>
    <div>
      <LadderWindowTableRow
        v-for="(ranker, index) in ladder.state.rankers"
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

const ladder = useLadderStore();

const lang = useLang("components.ladder.table");
const isScrolled = ref<boolean>(false);

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
