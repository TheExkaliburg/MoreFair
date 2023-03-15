<template>
  <div class="shrink">
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
      class="grid grid-cols-24 gap-1 px-1 px-1 font-bold text-text-light sticky top-0"
    >
      <div class="col-span-3">#</div>
      <div class="col-span-9">Username</div>
      <div class="col-span-6 text-right">Power</div>
      <div class="col-span-6 text-right">Points</div>
    </div>
    <div v-for="(ranker, index) in ladder.rankers" :key="ranker.accountId">
      <LadderWindowTableRow :index="Number(index)" :ranker="ranker" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useLadderStore } from "~/store/ladder";
import LadderWindowTableRow from "~/components/ladder/LadderWindowTableRow.vue";

const ladder = useLadderStore();
</script>

<style scoped></style>
