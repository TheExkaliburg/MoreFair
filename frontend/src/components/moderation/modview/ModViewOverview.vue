<template>
  <div class="flex flex-col justify-around h-full">
    <div v-if="false" class="grid grid-cols-4 gap-1">
      <div class="bg-gray-700 col-span-2 text-center rounded">Info 1</div>
      <div class="bg-gray-700 col-span-2 text-center rounded">Info 2</div>
      <div class="bg-gray-700 col-span-2 text-center rounded">Info 3</div>
      <div class="bg-gray-700 col-span-2 text-center rounded">...</div>
    </div>

    <div class="flex flex-col">
      <FairButton
        class="rounded-none text-left flex flex-row justify-between"
        @click="modViewStore.actions.changeView(ModViewTabType.Messages)"
      >
        <span class="">Messages</span>
        <span>
          <span class="text-xs opacity-80">{{
            overviewInformation?.messageLogCount ?? "?"
          }}</span>
          >
        </span>
      </FairButton>
      <FairButton
        class="rounded-none border-t-0 text-left flex flex-row justify-between"
        @click="modViewStore.actions.changeView(ModViewTabType.EventLog)"
        ><span class="">Event-Log</span>
        <span>
          <span class="text-xs opacity-80">{{
            overviewInformation?.eventLogCount ?? "?"
          }}</span>
          >
        </span></FairButton
      >
      <FairButton
        class="rounded-none border-t-0 text-left flex flex-row justify-between"
        @click="modViewStore.actions.changeView(ModViewTabType.AuditLog)"
        ><span class="">Audit-Log</span>
        <span>
          <span class="text-xs opacity-80">{{
            overviewInformation?.auditLogCount ?? "?"
          }}</span>
          >
        </span></FairButton
      >
      <FairButton
        class="rounded-none border-t-0 text-left flex flex-row justify-between"
        @click="modViewStore.actions.changeView(ModViewTabType.NameHistory)"
        ><span class="">Previous Names</span>
        <span>
          <span class="text-xs opacity-80">{{
            overviewInformation?.nameHistoryCount ?? "?"
          }}</span>
          >
        </span></FairButton
      >
      <FairButton
        class="rounded-none border-t-0 text-left flex flex-row justify-between"
        @click="modViewStore.actions.changeView(ModViewTabType.AltAccounts)"
        ><span class="">Accounts with same IP</span>
        <span>
          <span class="text-xs opacity-80">{{
            overviewInformation?.altAccountsCount ?? "?"
          }}</span>
          >
        </span></FairButton
      >
    </div>
  </div>
</template>

<script setup lang="ts">
import FairButton from "~/components/interactables/FairButton.vue";
import {
  InspectorOverviewInformation,
  ModViewTabType,
  useModViewStore,
} from "~/store/modView.store.ts";
import { computed } from "vue";
import { useModerationStore } from "~/store/moderation.ts";

const moderationStore = useModerationStore();
const modViewStore = useModViewStore();
const overviewInformation = computed<InspectorOverviewInformation | null>(
  () => modViewStore.state.overviewInformation,
);
</script>

<style scoped lang="scss"></style>
