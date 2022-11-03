<template>
  <div class="flex flex-row">
    <slot />
    <div
      class="hidden xl:block w-18 bg-navbar-bg flex flex-col justify-between px-2 py-2"
    >
      <div class="min-w-min flex flex-col justify-items-start content-around">
        <OffCanvasButton @click="uiStore.sidebarExpanded = false">
          <template #icon>
            <NuxtLink :to="authStore.homeLocation" class="flex">
              <img alt="FairGame" class="w-7 h-7" src="/favicon.ico" />
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
  </div>
</template>

<script lang="ts" setup>
import {
  ChatBubbleLeftEllipsisIcon,
  TableCellsIcon,
} from "@heroicons/vue/24/outline";
import { useUiStore } from "~/store/ui";
import OffCanvasButton from "~/components/navbar/OffCanvasSidebarButton.vue";
import { useAuthStore } from "~/store/authentication";

const uiStore = useUiStore();
const authStore = useAuthStore();
</script>

<style lang="scss" scoped></style>
