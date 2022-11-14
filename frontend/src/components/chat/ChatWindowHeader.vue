<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="chatStore.number"
      :max="10"
      :prefix="'Chad'"
      class="h-8 w-42 self-end"
      @change="changeChat"
    />
  </div>
</template>

<script lang="ts" setup>
import PaginationButtonGroup from "~/components/interactables/PaginationButtonGroup.vue";
import { useChatStore } from "~/store/chat";

const chatStore = useChatStore();

function changeChat(e: number) {
  chatStore.changeChat(e);
}
</script>
