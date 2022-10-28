<template>
  <div class="flex flex-col w-full">
    <div class="flex flex-row text-sm justify-between pr-3">
      <span class="text-text-light truncate basis-1/4 flex-auto">
        {{ message.username }}
        <sub class="text-text-dark">&nbsp;#{{ message.accountId }} </sub>
      </span>
      <span class="text-text-light basis-1/4 grow flex-auto"
        ><strong>{{ message.tag }}</strong
        ><sub class="text-text-dark">{{ message.ahPoints }}</sub></span
      >
      <span class="basis-1/4 grow flex-auto">
        {{ message.timestamp }}
      </span>
      <span v-if="true" class="text-end flex-none">...</span>
    </div>
    <ChatWindowContentMessageBody :message="message" />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessageBody from "~/components/chat/ChatWindowContentMessageBody.vue";

defineProps({
  message: {
    type: Object,
    required: true,
  },
});
</script>
