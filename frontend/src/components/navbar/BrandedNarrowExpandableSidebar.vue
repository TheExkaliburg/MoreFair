<template>
  <div class="flex flex-row">
    <div
      class="hidden xl:block w-18 bg-navbar-bg flex flex-col justify-between px-2 py-2"
    >
      <div class="min-w-min flex flex-col justify-items-start content-around">
        <OffCanvasButton @click="uiStore.sidebarExpanded = false">
          <template #icon>
            <NuxtLink to="/" class="flex">
              <img src="/favicon.ico" alt="FairGame" class="w-7 h-7" />
              <span class="text-xl text-navbar-text"></span>
            </NuxtLink>
          </template>
        </OffCanvasButton>
        <!--Leaving this spot free-->
        <OffCanvasButton class="cursor-auto" />
        <OffCanvasButton v-model="uiStore.ladderEnabled">
          <template #icon>
            <TableCellsIcon />
          </template>
        </OffCanvasButton>
        <OffCanvasButton v-model="uiStore.chatEnabled">
          <template #icon>
            <ChatBubbleLeftEllipsisIcon />
          </template>
        </OffCanvasButton>
      </div>
      <div
        class="min-w-min flex flex-col justify-items-start content-around"
      ></div>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
import {
  ChatBubbleLeftEllipsisIcon,
  TableCellsIcon,
} from "@heroicons/vue/24/outline";
import { useUiStore } from "~/store/ui";
import OffCanvasButton from "~/components/navbar/OffCanvasButton.vue";

const uiStore = useUiStore();
</script>

<style scoped lang="scss"></style>
