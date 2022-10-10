<template>
  <div
    ref="offCanvas"
    :class="
      uiStore.sidebarExpanded ? 'xl:translate-x-full' : 'translate-x-full'
    "
    class="flex flex-col bg-navbar-bg fixed inset-y-0 right-0 w-1/4 min-w-min max-w-full z-10 text-navbar-text px-4 py-1 transform transition-transform justify-between"
    tabindex="-1"
  >
    <!--Top of the Canvas-->
    <div class="min-w-min flex flex-col justify-items-start content-around">
      <OffCanvasSidebarButton
        :label="backLabel"
        @click="uiStore.sidebarExpanded = false"
      >
        <template #icon>
          <BackspaceIcon />
        </template>
      </OffCanvasSidebarButton>
      <!--Leaving this spot free-->
      <OffCanvasSidebarButton class="cursor-auto" />
      <OffCanvasSidebarButton
        v-model="uiStore.ladderEnabled"
        :label="ladderLabel"
      >
        <template #icon>
          <TableCellsIcon />
        </template>
      </OffCanvasSidebarButton>
      <OffCanvasSidebarButton v-model="uiStore.chatEnabled" :label="chatLabel">
        <template #icon>
          <ChatBubbleLeftEllipsisIcon />
        </template>
      </OffCanvasSidebarButton>
    </div>
    <!--Bottom of the Canvas-->
    <div class="min-w-min flex flex-col justify-items-start content-around">
      <NuxtLink to="/">
        <OffCanvasSidebarButton :label="optionsLabel">
          <template #icon>
            <Cog8ToothIcon />
          </template>
        </OffCanvasSidebarButton>
      </NuxtLink>
      <NuxtLink to="/">
        <OffCanvasSidebarButton :label="helpLabel">
          <template #icon>
            <QuestionMarkCircleIcon />
          </template>
        </OffCanvasSidebarButton>
      </NuxtLink>
      <NuxtLink to="/">
        <OffCanvasSidebarButton :label="discordLabel">
          <template #icon>
            <font-awesome-icon icon="fa-brands fa-discord" />
          </template>
        </OffCanvasSidebarButton>
      </NuxtLink>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {
  BackspaceIcon,
  ChatBubbleLeftEllipsisIcon,
  Cog8ToothIcon,
  QuestionMarkCircleIcon,
  TableCellsIcon,
} from "@heroicons/vue/24/outline";

import { computed, ref } from "vue";
import { useUiStore } from "~/store/ui";
import OffCanvasSidebarButton from "~/components/navbar/OffCanvasSidebarButton.vue";
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

uiStore.sidebarExpanded = false;

onClickOutside(offCanvas, () => {
  if (uiStore.sidebarExpanded) {
    uiStore.sidebarExpanded = false;
  }
});
</script>

<style lang="scss" scoped></style>
