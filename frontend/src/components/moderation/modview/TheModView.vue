<template>
  <aside
    class="h-full shrink-0 overflow-hidden border-text-dark bg-navbar-bg right-0 transform transition-[width,min-width] duration-200 ease-out text-text"
    :class="{
      'w-0 min-w-0 max-w-0': !modViewStore.getters.isOpen,
      'w-full min-w-96 sm:w-1/4  border-l-2': modViewStore.getters.isOpen,
    }"
  >
    <div class="h-full flex flex-col py-4 mx-4 overflow-hidden">
      <div class="flex flex-col pb-2">
        <div class="flex flex-row justify-between">
          <span class="text-text-light pl-4 font-semibold">Mod View</span>
          <button @click="modViewStore.actions.reload()">🔃</button>
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
        <div class="mt-1 grid grid-cols-4 gap-1">
          <FairButton class="rounded-t-none" @click="rename()"
            >Rename</FairButton
          >
          <FairButton class="rounded-t-none" @click="mute()">Mute</FairButton>
          <FairButton class="rounded-t-none" @click="ban()">Ban</FairButton>
          <FairButton class="rounded-t-none" @click="free()">Free</FairButton>
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
import { useModerationStore } from "~/store/moderation.ts";

const moderationStore = useModerationStore();
const modViewStore = useModViewStore();
const currentView = computed<ModViewTabType>(
  () => modViewStore.state.currentView,
);
const selectedPlayerId = computed<number | null>(
  () => modViewStore.state.selectedPlayerId,
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

function rename() {
  const username = prompt(
    "Please enter the new username:",
    overviewInformation.value?.displayName ?? "",
  );
  if (username && selectedPlayerId.value) {
    moderationStore.actions.rename(selectedPlayerId.value, username);
    modViewStore.actions.reload();
  }
}

function ban() {
  if (!selectedPlayerId.value) {
    return;
  }

  const confirmed = confirm(
    `Are you sure you want to BAN ${overviewInformation.value?.displayName ?? ""} #${selectedPlayerId.value}?`,
  );
  if (confirmed) {
    moderationStore.actions.ban(selectedPlayerId.value);
    modViewStore.actions.reload();
  }
}

function mute() {
  if (!selectedPlayerId.value) {
    return;
  }

  const confirmed = confirm(
    `Are you sure you want to MUTE ${overviewInformation.value?.displayName ?? ""} #${selectedPlayerId.value}?`,
  );
  if (confirmed) {
    moderationStore.actions.mute(selectedPlayerId.value);
    modViewStore.actions.reload();
  }
}

function free() {
  if (!selectedPlayerId.value) {
    return;
  }

  const confirmed = confirm(
    `Are you sure you want to FREE ${overviewInformation.value?.displayName ?? ""} #${selectedPlayerId.value}?`,
  );
  if (confirmed) {
    moderationStore.actions.free(selectedPlayerId.value);
    modViewStore.actions.reload();
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
