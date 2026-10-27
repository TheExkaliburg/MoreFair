<template>
  <div
    class="flex flex-col h-full w-full items-center text-button-text overflow-y-auto overflow-x-hidden py-2 z-[1]"
  >
    <ChatWindowContentMessage
      v-for="(m, i) in chatStore.getters.allMessages"
      :key="messageKey(m)"
      :index="i"
      :message="m"
      class="py-0.5"
    />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessage from "../../components/chat/ChatWindowContentMessage.vue";
import { useChatStore } from "~/store/chat";
import type { MessageView } from "~/store/entities/message";

const chatStore = await useChatStore();
const messageKeys = new WeakMap<MessageView, symbol>();

function messageKey(message: MessageView): symbol {
  let key = messageKeys.get(message);
  if (!key) {
    key = Symbol("chat-message");
    messageKeys.set(message, key);
  }
  return key;
}
</script>
