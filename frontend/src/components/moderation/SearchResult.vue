<template>
  <div class="w-full overflow-y-scroll text-text">
    {{
      moderationStore.state.searchType === SearchType.DISPLAY_NAME
        ? "Usernames:"
        : "Alt-Accounts:"
    }}
    <table class="w-full text-text table-auto">
      <tr class="text-text-light text-left">
        <th>ID</th>
        <th>DisplayName</th>
        <th class="text-right">Last Login</th>
        <th class="text-right"></th>
      </tr>
      <tr
        v-for="result in moderationStore.state.searchResults"
        :key="result.accountId"
      >
        <td>{{ result.accountId }}</td>
        <td>{{ result.displayName }}</td>
        <td class="text-right">
          {{ useDateFormatter(result.lastLogin, dateFormatOptions) }}
        </td>
        <td class="text-center hover:text-text-light active:text-text-dark">
          <button @click="copyIntoSearch(result)">Copy</button>
        </td>
      </tr>
    </table>
  </div>
</template>

<script setup lang="ts">
import {
  SearchType,
  useModerationStore,
  UserSearchResult,
} from "~/store/moderation";
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

function copyIntoSearch(result: UserSearchResult) {
  moderationStore.state.usernameSearchInput = result.displayName;
  moderationStore.state.accountIdSearchInput = String(result.accountId);
}
</script>

<style scoped lang="scss"></style>
