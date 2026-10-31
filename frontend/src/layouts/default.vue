<template>
  <div class="flex flex-col h-screen overflow-hidden">
    <TheBrandedNavBarWithOffCanvas />
    <TheSidebar class="restOfVerticalScreen">
      <div
        class="restOfHorizontalScreen max-[640px]:w-screen flex-row flex"
        @click="uiStore.state.sidebarExpanded = false"
      >
        <div class="min-w-0 flex-1">
          <slot />
        </div>
        <TheModView />
      </div>
    </TheSidebar>
  </div>
</template>

<script lang="ts" setup>
import TheSidebar from "../components/navbar/TheSidebar.vue";
import TheBrandedNavBarWithOffCanvas from "../components/navbar/TheBrandedNavBarWithOffCanvas.vue";
import { useUiStore } from "~/store/ui";
import TheModView from "../components/moderation/modview/TheModView.vue";
import { useModViewStore } from "~/store/modView.store.ts";

const modViewStore = useModViewStore();
const uiStore = useUiStore();
</script>

<style lang="scss" scoped>
.restOfVerticalScreen {
  height: calc(100vh - 2.5rem);
}

.restOfHorizontalScreen {
  @media (min-width: 640px) {
    width: calc(100vw - 2.5rem);
    max-width: calc(100vw - 2.5rem);
  }
}
</style>
