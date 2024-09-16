<template>
  <div class="flex flex-col gap-2 text-text">
    <div class="flex flex-row">
      <FairInput
        v-model="moderationStore.state.usernameSearchInput"
        class="rounded-r-none w-full"
        placeholder="Username"
        @keydown.enter.prevent="searchUsername"
      />
      <FairButton
        class="border-l-0 rounded-l-none w-1/6"
        @click="searchUsername"
        >Search
      </FairButton>
    </div>
    <div class="flex flex-row">
      <FairInput
        v-model="moderationStore.state.accountIdSearchInput"
        class="rounded-r-none w-full"
        placeholder="Account-ID"
        @keydown.enter.prevent="searchAccountId"
      />
      <FairButton
        class="border-l-0 rounded-l-none w-1/6"
        @click="searchAccountId"
        >Search
      </FairButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import FairButton from "~/components/interactables/FairButton.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import { SearchType, useModerationStore } from "~/store/moderation";

const moderationStore = useModerationStore();

function searchUsername() {
  moderationStore.actions.search(SearchType.USERNAME);
}

function searchAccountId() {
  moderationStore.actions.search(SearchType.ACCOUNT_ID);
}
</script>
