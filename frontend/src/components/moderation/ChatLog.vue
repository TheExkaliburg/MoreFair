<template>
  <div
    class="flex flex-col h-full w-full items-center text-button-text overflow-y-auto overflow-x-hidden py-2 z-1"
  >
    <div class="text-left w-full">Chat Log:</div>
    <div class="h-4 w-full lg:hidden">&nbsp;</div>
    <ChatWindowContentMessage
      v-for="(m, i) in moderationStore.getters.allMessages"
      :key="i"
      :index="i"
      :message="m"
      class="py-1"
    />
  </div>
</template>

<script lang="ts" setup>
import { useModerationStore } from "~/store/moderation";
import ChatWindowContentMessage from "~/components/chat/ChatWindowContentMessage.vue";

const moderationStore = useModerationStore();
</script>

<style lang="scss" scoped></style>
