<template>
  <div
    ref="canvas"
    class="flex flex-col bg-navbar-bg fixed inset-y-0 right-0 w-4/12 min-w-min max-w-prose z-10 text-navbar-text p-4 transform transition-transform justify-between"
    :class="
      uiStore.sidebarExpanded ? 'md:translate-x-full' : 'translate-x-full'
    "
    tabindex="-1"
  >
    <!--Top of the Canvas-->
    <div class="min-w-min flex flex-col justify-items-start content-around">
      <OffCanvasButton label="Back" @click="uiStore.sidebarExpanded = false">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Back1" @click="uiStore.sidebarExpanded = false">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Back12" @click="uiStore.sidebarExpanded = false">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Back123" @click="uiStore.sidebarExpanded = false">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
    </div>
    <!--Bottom of the Canvas-->
    <div
      class="min-w-min flex flex-col-reverse justify-items-start content-around"
    >
      <OffCanvasButton label="Back9" @click="uiStore.sidebarExpanded = false">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Back98" @click="uiStore.sidebarExpanded = false">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { BackspaceIcon } from "@heroicons/vue/24/outline";
import { useUiStore } from "~/store/ui";
import OffCanvasButton from "~/components/Navbar/OffCanvasButton.vue";
import { onClickOutside } from "#imports";

const uiStore = useUiStore();
const canvas = ref<HTMLElement | null>(null);

onClickOutside(canvas, () => {
  if (uiStore.sidebarExpanded) {
    uiStore.sidebarExpanded = false;
  }
});
</script>

<style scoped lang="scss"></style>
