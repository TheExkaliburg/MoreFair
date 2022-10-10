<template>
  <div class="w-full h-screen flex flex-col lg:flex-row">
    <LadderWindow
      v-if="uiStore.ladderEnabled"
      :class="uiStore.chatEnabled ? 'h-2/3 lg:w-3/5' : 'h-full lg:w-full'"
      class="w-full p-1 lg:h-full"
    />
    <ChatWindow
      v-if="uiStore.chatEnabled"
      :class="uiStore.ladderEnabled ? 'h-1/3 lg:w-2/5' : 'h-full lg:w-full'"
      class="w-full p-1 lg:h-full"
    />
    <!--TheLoginDialog></TheLoginDialog-->
  </div>
</template>

<script lang="ts" setup>
import { useUiStore } from "~/store/ui";

const uiStore = useUiStore();
</script>
