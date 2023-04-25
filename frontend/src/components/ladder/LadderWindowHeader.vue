<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="ladderStore.state.number"
      :max="3"
      :prefix="'Ladder'"
      class="h-8 w-42 self-end bg-background z-2"
      @change="(number) => ladderStore.actions.changeLadder(number)"
    />
  </div>
</template>

<script lang="ts" setup>
import PaginationButtonGroup from "../../components/interactables/PaginationButtonGroup.vue";
import { useLadderStore } from "~/store/ladder";

const ladderStore = useLadderStore();
</script>
