<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="chatStore.state.number"
      :max="accountStore.state.highestCurrentLadder"
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

const chatStore = useChatStore();
const accountStore = useAccountStore();
</script>
