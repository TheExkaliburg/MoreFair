<template>
  <div
    ref="canvas"
    class="flex flex-col bg-navbar-bg fixed inset-y-0 right-0 w-4/12 min-w-min max-w-prose z-10 text-navbar-text px-4 py-1 transform transition-transform justify-between"
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
      <!--Leaving this spot free-->
      <OffCanvasButton class="cursor-auto" />
      <OffCanvasButton
        label="Ladder"
        :toggle="uiStore.ladderEnabled"
        @onToggle="(value) => (uiStore.ladderEnabled = value)"
      >
        <template #icon>
          <TableCellsIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Chat">
        <template #icon>
          <ChatBubbleLeftEllipsisIcon />
        </template>
      </OffCanvasButton>
    </div>
    <!--Bottom of the Canvas-->
    <div class="min-w-min flex flex-col justify-items-start content-around">
      <OffCanvasButton label="Options">
        <template #icon>
          <Cog8ToothIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Help">
        <template #icon>
          <QuestionMarkCircleIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton label="Discord">
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  BackspaceIcon,
  ChatBubbleLeftEllipsisIcon,
  Cog8ToothIcon,
  QuestionMarkCircleIcon,
  TableCellsIcon,
} from "@heroicons/vue/24/outline";
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
