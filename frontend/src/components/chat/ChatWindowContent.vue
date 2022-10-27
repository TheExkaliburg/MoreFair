<template>
  <div class="flex justify-center items-center text-button-text h-full">
    <ChatWindowContentMessageBody v-for="m in messages" :key="m" :message="m" />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessageBody from "~/components/chat/ChatWindowContentMessageBody.vue";

const message = { message: "test {@}", metadata: [{ u: "Tobi", i: 5, id: 1 }] };
const messages = ref<Object[]>([message, message, message]);
</script>
