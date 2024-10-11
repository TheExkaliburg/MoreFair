<template>
  <div class="w-full overflow-y-scroll text-text">
    <table class="w-full text-text table-auto">
      <tr class="text-text-light text-left">
        <th>ID</th>
        <th>Current Name</th>
        <th>Previous Name</th>
        <th class="text-right">Timestamp</th>
        <th class="text-right"></th>
      </tr>
      <tr
        v-for="(result, index) in moderationStore.state.nameChangeLog"
        :key="index"
      >
        <td>{{ result.accountId }}</td>
        <td>{{ result.currentName }}</td>
        <td>{{ result.displayName }}</td>
        <td class="text-right">
          {{ useDateFormatter(result.timestamp, dateFormatOptions) }}
        </td>
        <td class="text-center hover:text-text-light active:text-text-dark">
          <button @click="copyIntoSearch(result)">Copy</button>
        </td>
      </tr>
    </table>
  </div>
</template>

<script setup lang="ts">
import { NameChange, useModerationStore } from "~/store/moderation";
import { useDateFormatter } from "~/composables/useFormatter";

const moderationStore = useModerationStore();

const dateFormatOptions: Intl.DateTimeFormatOptions = {
  weekday: "short",
  day: "numeric",
  month: "numeric",
  hour: "numeric",
  minute: "numeric",
  hourCycle: "h23",
  year: "2-digit",
};

function copyIntoSearch(result: NameChange) {
  moderationStore.state.usernameSearchInput = result.displayName;
  moderationStore.state.accountIdSearchInput = String(result.accountId);
}
</script>

<style scoped lang="scss"></style>
