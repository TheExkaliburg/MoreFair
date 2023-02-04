<template>
  <div class="w-full h-full flex flex-col lg:flex-row bg-background">
    <LadderWindow
      v-if="uiStore.state.ladderEnabled"
      :class="
        uiStore.state.chatEnabled ? 'h-2/3 lg:w-7/10' : 'h-full lg:w-full'
      "
      class="w-full lg:h-full shrink-0"
    />
    <ChatWindow
      v-if="uiStore.state.chatEnabled"
      :class="[
        uiStore.state.ladderEnabled ? 'h-1/3 lg:w-3/10' : 'h-full lg:w-full',
        {
          'border-t-1':
            uiStore.state.ladderEnabled && uiStore.state.chatEnabled,
        },
      ]"
      class="w-full lg:h-full border-button-border lg:border-t-0 shrink-0"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from "vue";
import { useUiStore } from "~/store/ui";
import { useStomp } from "~/composables/useStomp";
import { useChatStore } from "~/store/chat";
import { useLadderStore } from "~/store/ladder";
import { useTutorialTour } from "~/composables/useTour";

const uiStore = useUiStore();

definePageMeta({ layout: "default" });

useStomp();

onMounted(() => {
  useChatStore().init();
  useLadderStore().init();
  const tour = useTutorialTour();
  if (!tour.getFlag()) {
    tour.start();
  }
});
</script>
