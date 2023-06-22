<template>
  <div
    class="flex flex-col h-full w-full items-center text-button-text overflow-y-auto overflow-x-hidden py-2 z-1"
  >
    <div class="h-4 w-full lg:hidden">&nbsp;</div>
    <ChatLogMessage
      v-for="(m, i) in moderationStore.state.chatLog"
      :key="m"
      :index="i"
      :message="m"
      class="py-1"
    />
  </div>
</template>

<script lang="ts" setup>
import ChatLogMessage from "~/components/moderation/ChatLogMessage.vue";
import { useModerationStore } from "~/store/moderation";

const moderationStore = useModerationStore();
</script>

<style lang="scss" scoped></style>
