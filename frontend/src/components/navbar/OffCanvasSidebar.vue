<template>
  <div
    ref="offCanvas"
    class="flex flex-col bg-navbar-bg fixed inset-y-0 right-0 w-1/4 min-w-min max-w-full z-10 text-navbar-text px-4 py-1 transform transition-transform justify-between"
    :class="
      uiStore.sidebarExpanded ? 'xl:translate-x-full' : 'translate-x-full'
    "
    tabindex="-1"
  >
    <!--Top of the Canvas-->
    <div class="min-w-min flex flex-col justify-items-start content-around">
      <OffCanvasButton
        :label="backLabel"
        @click="uiStore.sidebarExpanded = false"
      >
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasButton>
      <!--Leaving this spot free-->
      <OffCanvasButton class="cursor-auto" />
      <OffCanvasButton v-model="uiStore.ladderEnabled" :label="ladderLabel">
        <template #icon>
          <TableCellsIcon />
        </template>
      </OffCanvasButton>
      <OffCanvasButton v-model="uiStore.chatEnabled" :label="chatLabel">
        <template #icon>
          <ChatBubbleLeftEllipsisIcon />
        </template>
      </OffCanvasButton>
    </div>
    <!--Bottom of the Canvas-->
    <div class="min-w-min flex flex-col justify-items-start content-around">
      <NuxtLink to="/">
        <OffCanvasButton :label="optionsLabel">
          <template #icon>
            <Cog8ToothIcon />
          </template>
        </OffCanvasButton>
      </NuxtLink>
      <NuxtLink to="/">
        <OffCanvasButton :label="helpLabel">
          <template #icon>
            <QuestionMarkCircleIcon />
          </template>
        </OffCanvasButton>
      </NuxtLink>
      <NuxtLink to="/">
        <OffCanvasButton :label="discordLabel">
          <template #icon>
            <font-awesome-icon icon="fa-brands fa-discord" />
          </template>
        </OffCanvasButton>
      </NuxtLink>
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

import { computed, ref } from "vue";
import { useUiStore } from "~/store/ui";
import OffCanvasButton from "~/components/navbar/OffCanvasButton.vue";
import { onClickOutside, useLang } from "#imports";

const uiStore = useUiStore();
const offCanvas = ref<HTMLElement | null>(null);

const lang = useLang("components.navbar.offcanvas-sidebar");
const backLabel = computed<string>(() => lang("back"));
const ladderLabel = computed<string>(() => lang("ladder"));
const chatLabel = computed<string>(() => lang("chat"));
const optionsLabel = computed<string>(() => lang("options"));
const helpLabel = computed<string>(() => lang("help"));
const discordLabel = computed<string>(() => lang("discord"));

onClickOutside(offCanvas, () => {
  if (uiStore.sidebarExpanded) {
    uiStore.sidebarExpanded = false;
  }
});
</script>

<style scoped lang="scss"></style>
