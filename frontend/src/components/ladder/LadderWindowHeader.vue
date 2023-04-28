<template>
  <div class="text-xs flex justify-between items-center text-button-text">
    <div class="whitespace-nowrap">
      Active Rankers: {{ ladderStore.getters.activeRankers }} /
      {{ ladderStore.state.rankers.length }}
    </div>
    <div class="whitespace-nowrap">
      Ladders: {{ accountStore.state.highestCurrentLadder }} /
      {{ roundStore.state.assholeLadder }} ({{ roundStore.state.topLadder }})
    </div>
    <div class="whitespace-nowrap">
      Round: [{{ roundStore.getters.formattedTypes }}]
    </div>
    <div class="whitespace-nowrap">
      Ladder: [{{ ladderStore.getters.formattedTypes }}]
    </div>
    <PaginationButtonGroup
      :current="ladderStore.state.number"
      :max="accountStore.state.highestCurrentLadder"
      :prefix="'Ladder'"
      class="h-8 w-42 self-end bg-background z-2 text-base"
      :last="roundStore.state.assholeLadder"
      :show-last="isAssholeLadderOpen"
      @change="(number) => ladderStore.actions.changeLadder(number)"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import PaginationButtonGroup from "../../components/interactables/PaginationButtonGroup.vue";
import { useLadderStore } from "~/store/ladder";
import { useAccountStore } from "~/store/account";
import { useRoundStore } from "~/store/round";

const ladderStore = useLadderStore();
const roundStore = useRoundStore();
const accountStore = useAccountStore();

const isAssholeLadderOpen = computed<boolean>(
  () => roundStore.state.topLadder >= roundStore.state.assholeLadder
);
</script>
