<template>
  <div class="flex flex-col text-text">
    <div>Take Mod Action:</div>
    <div class="flex flex-row">
      <FairInput
        v-model="input"
        class="w-full rounded-b-none border-b-0"
        placeholder="Account-ID"
        @keydown.enter.prevent
      />
    </div>
    <div class="flex flex-row">
      <FairButton class="rounded-r-none rounded-t-none w-full" @click="ban"
        >Ban
      </FairButton>
      <FairButton
        class="border-l-0 rounded-l-none rounded-r-none w-full"
        @click="mute"
        >Mute
      </FairButton>
      <FairButton
        class="border-l-0 rounded-l-none rounded-r-none w-full"
        @click="rename"
        >Rename
      </FairButton>
      <FairButton
        class="border-l-0 rounded-l-none rounded-r-none w-full"
        @click="free"
        >Free
      </FairButton>
      <FairButton
        v-if="accountStore.state.accessRole === AccessRole.OWNER"
        class="border-l-0 rounded-t-none rounded-l-none w-full"
        @click="mod"
        >Mod
      </FairButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useModerationStore } from "~/store/moderation";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { AccessRole, useAccountStore } from "~/store/account";

const input = ref<string>("");

const accountStore = useAccountStore();
const moderationStore = useModerationStore();

function ban() {
  if (confirmAction("ban")) {
    moderationStore.actions.ban(parseInput());
  }
}

function mute() {
  if (confirmAction("mute")) {
    moderationStore.actions.mute(parseInput());
  }
}

function rename() {
  const username = prompt("Please enter the new username:");

  if (username && confirmAction("rename")) {
    moderationStore.actions.rename(parseInput(), username);
  }
}

function free() {
  if (confirmAction("free")) {
    moderationStore.actions.free(parseInput());
  }
}

function mod() {
  if (confirmAction("mod")) {
    moderationStore.actions.mod(parseInput());
  }
}

function parseInput() {
  return parseInt(input.value);
}

function confirmAction(actionName: string) {
  if (input.value === "") return false;

  return confirm(
    `Are you sure you want to ${actionName} the user with the account-id #${input.value}?`,
  );
}
</script>

<style scoped lang="scss"></style>
