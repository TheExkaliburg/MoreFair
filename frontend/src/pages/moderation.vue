<template>
  <div class="grid grid-cols-1 gap-1 gap-y-2 overflow-y-auto h-auto p-4">
    <!--div class="bg-yellow-300 h-full max-h-96">Event-Log</div-->
    <!--div class="bg-red-300 h-full max-h-96">Search Usernames + Table</div-->
    <SearchUsername />
    <!--div class="bg-orange-300 h-full max-h-96">
      Search Alt Accounts of ID + Table
    </div-->
    <TakeModAction />
    <SearchAltAccounts />
    <!--div class="bg-blue-300 h-full max-h-128">
      Search Rename History + Table
    </div-->
    <ChatLog class="h-full max-h-96" />
  </div>
</template>

<script lang="ts" setup>
import { navigateTo } from "nuxt/app";
import { useAccountStore } from "~/store/account";
import ChatLog from "~/components/moderation/ChatLog.vue";
import { useModerationStore } from "~/store/moderation";
import SearchUsername from "~/components/moderation/SearchUsername.vue";
import TakeModAction from "~/components/moderation/TakeModAction.vue";
import SearchAltAccounts from "~/components/moderation/SearchAltAccounts.vue";

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
