<template>
  <div class="flex flex-col text-text">
    <div>Search Alt-Accounts:</div>
    <div class="flex flex-row">
      <FairInput
        v-model="input"
        class="rounded-r-none w-full"
        placeholder="Account-ID"
        @keydown.enter.prevent="searchAlt"
      />
      <FairButton class="border-l-0 rounded-l-none px-4" @click="searchAlt"
        >Search
      </FairButton>
    </div>
    <div>Result: {{ moderationStore.state.altSearchResults }}</div>
  </div>
</template>

<script setup lang="ts">
import { useModerationStore } from "~/store/moderation";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";

const input = ref<string>("");

const moderationStore = useModerationStore();

function searchAlt() {
  const accountId = parseInt(input.value);
  moderationStore.actions.searchAltAccounts(accountId);
}
</script>

<style scoped lang="scss"></style>
