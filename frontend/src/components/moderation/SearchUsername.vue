<template>
  <div class="flex flex-col text-text">
    <div>Search Username:</div>
    <div class="flex flex-row">
      <FairInput
        v-model="input"
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
    <div>Result: {{ moderationStore.state.searchResults }}</div>
  </div>
</template>

<script setup lang="ts">
import { useModerationStore } from "~/store/moderation";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";

const input = ref<string>("");

const moderationStore = useModerationStore();

function searchUsername() {
  moderationStore.actions.searchUsername(input.value);
}
</script>

<style scoped lang="scss"></style>
