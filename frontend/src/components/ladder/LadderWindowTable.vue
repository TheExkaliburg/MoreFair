<template>
  <div>
    <DynamicScroller
      :buffer="1000"
      :items="shownRanker"
      :min-item-size="23"
      key-field="rank"
      page-mode
    >
      <template #before>
        <div class="flex flex-row flex-nowrap justify-between items-center">
          <div class="w-full">#</div>
          <div class="w-full">Username</div>
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
          :size-dependencies="item"
        >
          <LadderWindowTableRow :ranker="item" />
        </DynamicScrollerItem>
      </template>
    </DynamicScroller>
  </div>
</template>

<script lang="ts" setup>
import {
  DynamicScroller,
  DynamicScrollerItem,
} from "vue-virtual-scroller/dist/vue-virtual-scroller.esm";
import { computed } from "vue";
import { useLadderStore } from "~/store/ladder";
import LadderWindowTableRow from "~/components/ladder/LadderWindowTableRow.vue";

const shownRanker = computed(() => [...ladder.rankers]);

const ladder = useLadderStore();
</script>

<style scoped></style>
