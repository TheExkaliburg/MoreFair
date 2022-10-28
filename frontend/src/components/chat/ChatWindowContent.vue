<template>
  <div
    class="flex flex-col h-full w-full items-center text-button-text overflow-y-auto overflow-x-hidden py-2"
  >
    <ChatWindowContentMessage
      v-for="m in messages"
      :key="m"
      :message="m"
      class="py-1"
    />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessage from "~/components/chat/ChatWindowContentMessage.vue";

const message = {
  username: "Chad Lo",
  message:
    "This text has a mention ({@}) and a group mention inside it! ({$}) Also this long line of content goes after it : kasnduoahsdonasjkdnsdöafölasdkjfpoasndfklasdjnfasödlkjflasknfölkasnfklnma. Hope that didn't cause any weird layout issues",
  tag: "♡",
  ahPoints: 5950,
  accountId: 11235,
  timestamp: "Tue 24:00",
  metadata: [
    { u: "Tobi", i: 25, id: 11235 },
    { g: "group", i: 62 },
  ],
  flags: [],
};
const messages = ref<Object[]>([message, message, message]);
</script>
