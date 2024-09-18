<template>
  <div class="grid grid-cols-2 gap-1 gap-y-2 overflow-y-auto h-full p-4">
    <SearchUserInput />
    <TakeModAction />
    <SearchResult />
    <NamingHistory />
    <ChatLog class="h-full max-h-96 row-span-2" />
    <div class="bg-red-300 h-full max-h-128">Event Log</div>
  </div>
</template>

<script lang="ts" setup>
import { navigateTo } from "nuxt/app";
import { useAccountStore } from "~/store/account";
import ChatLog from "~/components/moderation/ChatLog.vue";
import { useModerationStore } from "~/store/moderation";
import SearchResult from "~/components/moderation/SearchResult.vue";
import TakeModAction from "~/components/moderation/TakeModAction.vue";
import SearchUserInput from "~/components/moderation/SearchUserInput.vue";
import NamingHistory from "~/components/moderation/NamingHistory.vue";

useSeoMeta({
  title: "FairGame - Moderation",
  description: "The Moderation Page of the FairGame Website",
});

const moderationStore = useModerationStore();

onMounted(() => {
  useAccountStore()
    .actions.init()
    .then(() => {
      if (!useAccountStore().getters.isMod) {
        navigateTo("/");
      }
      moderationStore.actions.init();
    });
});
</script>

<style lang="scss" scoped></style>
