<template>
  <aside
    class="h-full shrink-0 overflow-hidden border-text-dark bg-navbar-bg right-0 transform transition-[width,min-width] duration-200 ease-out text-text"
    :class="{
      'w-0 min-w-0 max-w-0': !modViewStore.getters.isOpen,
      'w-full min-w-96 sm:w-1/4  border-l-2': modViewStore.getters.isOpen,
    }"
    @keydown.esc="close()"
  >
    <div class="h-full flex flex-col py-4 mx-4 overflow-hidden">
      <div class="flex flex-col">
        <div class="flex flex-row justify-between">
          <span class="text-text-light pl-4 font-semibold">Mod View</span>
          <button>🔃</button>
          <button class="m-1 p-1 px-2 text-xl rotate-180" @click="close()">
            x
          </button>
        </div>
        <div
          class="text-2xl grid grid-cols-3 border-button-border border rounded-t-lg"
        >
          <span class="ml-2 justify-self-start col-span-2">{{
            overviewInformation?.displayName ?? "Unknown"
          }}</span
          ><span class="justify-self-center text-text-dark"
            >#{{ modViewStore.state.selectedPlayerId ?? 0 }}</span
          >
        </div>
        <div class="mt-1 grid grid-cols-5 gap-1">
          <FairButton class="rounded-t-none" disabled>Rename</FairButton>
          <FairButton class="rounded-t-none" disabled>Audit</FairButton>
          <FairButton class="rounded-t-none" disabled>Mute</FairButton>
          <FairButton class="rounded-t-none" disabled>Ban</FairButton>
          <FairButton class="rounded-t-none" disabled>Free</FairButton>
        </div>
      </div>
      <div
        v-if="modViewStore.getters.isOpen"
        class="flex flex-row w-[600%] h-full"
      >
        <ModViewOverview
          class="w-1/2 transform transition-transform ease-out"
          :class="{
            '-translate-x-full': currentView !== ModViewTabType.Overview,
          }"
        />
        <ModViewMessages
          class="w-1/2 transform transition-transform ease-out"
          :class="{
            '-translate-x-full': currentView === ModViewTabType.Messages,
          }"
        />
        <ModViewEventLog
          class="w-1/2 transform transition-transform ease-out -translate-x-full"
          :class="{
            '-translate-x-[200%]': currentView === ModViewTabType.EventLog,
          }"
        />
        <ModViewAuditLog
          class="w-1/2 transform transition-transform ease-out -translate-x-[200%]"
          :class="{
            '-translate-x-[300%]': currentView === ModViewTabType.AuditLog,
          }"
        />
        <ModViewNameHistory
          class="w-1/2 transform transition-transform ease-out -translate-x-[300%]"
          :class="{
            '-translate-x-[400%]': currentView === ModViewTabType.NameHistory,
          }"
        />
        <ModViewAltAccounts
          class="w-1/2 transform transition-transform ease-out -translate-x-[400%]"
          :class="{
            '-translate-x-[500%]': currentView === ModViewTabType.AltAccounts,
          }"
        />
      </div>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import {
  InspectorOverviewInformation,
  ModViewTabType,
  useModViewStore,
} from "~/store/modView.store.ts";
import { computed } from "vue";
import ModViewOverview from "~/components/moderation/modview/ModViewOverview.vue";
import ModViewMessages from "~/components/moderation/modview/ModViewMessages.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import ModViewEventLog from "~/components/moderation/modview/ModViewEventLog.vue";
import ModViewAuditLog from "~/components/moderation/modview/ModViewAuditLog.vue";
import ModViewNameHistory from "~/components/moderation/modview/ModViewNameHistory.vue";
import ModViewAltAccounts from "~/components/moderation/modview/ModViewAltAccounts.vue";

const modViewStore = useModViewStore();
const currentView = computed<ModViewTabType>(
  () => modViewStore.state.currentView,
);
const overviewInformation = computed<InspectorOverviewInformation | null>(
  () => modViewStore.state.overviewInformation,
);

function close() {
  if (modViewStore.state.currentView === ModViewTabType.Overview) {
    modViewStore.actions.close();
  } else {
    modViewStore.state.currentView = ModViewTabType.Overview;
  }
}
</script>

<style lang="scss" scoped>
div,
span,
button {
  #border: white solid 1px;
}
</style>
