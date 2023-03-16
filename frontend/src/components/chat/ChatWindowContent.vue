<template>
  <div
    class="flex flex-col h-full w-full items-center text-button-text overflow-y-auto overflow-x-hidden py-2 z-1"
  >
    <div class="h-4 w-full lg:hidden">&nbsp;</div>
    <ChatWindowContentMessage
      v-for="m in chatStore.messages"
      :key="m"
      :message="m"
      class="py-1"
    />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessage from "../../components/chat/ChatWindowContentMessage.vue";
import { useChatStore } from "~/store/chat";

const chatStore = await useChatStore();
</script>
