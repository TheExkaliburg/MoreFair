<template>
  <nav class="bg-navbar-bg">
    <div class="mx-auto px-2">
      <div class="flex h-10 items-center justify-between">
        <BrandedSidebarToggle class="z-15" />
        <div class="flex space-x-1 items-center justify-end">
          <ToggleIconButton
            v-model="optionsStore.state.ladder.lockButtons.value"
            v-tippy="{ content: lang('lockButtons'), placement: 'bottom' }"
            aria-label="lock buttons"
          >
            <template #icon>
              <LockClosedIcon />
            </template>
            <template #selectedIcon>
              <LockClosedIconSolid />
            </template>
          </ToggleIconButton>
          <ToggleIconButton
            v-model="uiStore.state.ladderEnabled"
            v-tippy="{ content: lang('enableLadder'), placement: 'bottom' }"
            aria-label="enable ladder view"
          >
            <template #icon>
              <TableCellsIcon />
            </template>
            <template #selectedIcon>
              <TableCellsIconSolid />
            </template>
          </ToggleIconButton>
          <ToggleIconButton
            v-model="uiStore.state.chatEnabled"
            v-tippy="{ content: lang('enableChat'), placement: 'bottom' }"
            aria-label="enable chat view"
          >
            <template #icon>
              <ChatBubbleLeftEllipsisIcon />
            </template>
            <template #selectedIcon>
              <ChatBubbleLeftEllipsisIconSolid />
            </template>
          </ToggleIconButton>
        </div>
      </div>
    </div>

    <OffCanvasBrandedSidebar />
  </nav>
</template>

<script lang="ts" setup>
import {
  ChatBubbleLeftEllipsisIcon,
  LockClosedIcon,
  TableCellsIcon,
} from "@heroicons/vue/24/outline";
import {
  ChatBubbleLeftEllipsisIcon as ChatBubbleLeftEllipsisIconSolid,
  LockClosedIcon as LockClosedIconSolid,
  TableCellsIcon as TableCellsIconSolid,
} from "@heroicons/vue/24/solid";
import { useLang } from "~/composables/useLang";
import OffCanvasBrandedSidebar from "~/components/navbar/OffCanvasSidebar.vue";
import { useUiStore } from "~/store/ui";
import ToggleIconButton from "~/components/ToggleIconButton.vue";
import BrandedSidebarToggle from "~/components/navbar/BrandedSidebarToggle.vue";
import { useOptionsStore } from "~/store/options";

const lang = useLang("components.navbar");

const uiStore = useUiStore();
const optionsStore = useOptionsStore();
</script>

<style lang="scss" scoped></style>
