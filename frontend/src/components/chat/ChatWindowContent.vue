<template>
  <div
    class="flex flex-col h-full w-full items-center text-button-text overflow-y-auto overflow-x-hidden py-2 z-1"
  >
    <div class="h-4 w-full lg:hidden">&nbsp;</div>
    <ChatWindowContentMessage
      v-for="(m, i) in chatStore.getters.allMessages"
      :key="m"
      :index="i"
      :message="m"
      class="py-0.5"
    />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessage from "../../components/chat/ChatWindowContentMessage.vue";
import { useChatStore } from "~/store/chat";

const chatStore = await useChatStore();
</script>
