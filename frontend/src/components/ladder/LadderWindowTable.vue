<template>
  <div class="shrink">
    <DynamicScroller
      :buffer="1000"
      :items="ladder.rankers"
      :min-item-size="23"
      class="max-h-full overflow-y-scroll text-sm"
      key-field="rank"
      page-mode
    >
      <template #before>
        <div
          class="flex flex-row flex-nowrap justify-between items-center px-1 font-bold text-text-light"
        >
          <div class="w-full">#</div>
          <div class="w-full">Username</div>
          <div class="w-full text-right">Power/s</div>
          <div class="w-full text-right">Power</div>
          <div class="w-full text-right">Points</div>
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
    </DynamicScroller>
    <!--LadderWindowTableRow
      v-for="(ranker, index) in shownRanker"
      :key="index"
      :active="true"
      :index="index"
      :ranker="ranker"
    /-->
  </div>
</template>

<script lang="ts" setup>
import { useLadderStore } from "~/store/ladder";
import LadderWindowTableRow from "~/components/ladder/LadderWindowTableRow.vue";

const ladder = useLadderStore();
</script>

<style scoped></style>
