<template>
  <div class="w-full h-screen flex flex-col lg:flex-row">
    <LadderWindow
      v-if="uiStore.ladderEnabled"
      class="w-full p-1 lg:h-full"
      :class="uiStore.chatEnabled ? 'h-2/3 lg:w-3/5' : 'h-full lg:w-full'"
    />
    <ChatWindow
      v-if="uiStore.chatEnabled"
      class="w-full p-1 lg:h-full"
      :class="uiStore.ladderEnabled ? 'h-1/3 lg:w-2/5' : 'h-full lg:w-full'"
    />
  </div>
</template>

<script setup lang="ts">
import { useUiStore } from "~/store/ui";

const uiStore = useUiStore();
</script>
