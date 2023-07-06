<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="chatStore.state.number"
      :max="maxChat"
      :prefix="'Chad'"
      class="h-8 w-42 self-end bg-background z-2"
      @change="(number) => chatStore.actions.changeChat(number)"
    />
  </div>
</template>

<script lang="ts" setup>
import PaginationButtonGroup from "../../components/interactables/PaginationButtonGroup.vue";
import { useChatStore } from "~/store/chat";
import { useAccountStore } from "~/store/account";
import { useRoundStore } from "~/store/round";

const chatStore = useChatStore();
const accountStore = useAccountStore();
const roundStore = useRoundStore();

const maxChat = computed<number>(() =>
  accountStore.getters.isMod
    ? roundStore.state.assholeLadder + 1
    : accountStore.state.highestCurrentLadder
);
</script>
