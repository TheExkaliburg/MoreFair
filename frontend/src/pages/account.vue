<template>
  <div
    class="flex flex-col w-full h-full justify-center items-center gap-8 text-text"
  >
    <div class="max-w-reader/2 w-full flex flex-col">
      <div>{{ lang("account") }}:</div>
      <div class="flex flex-row">
        <FairInput
          :class="{ 'pointer-events-none': !isEditingUsername }"
          :model-value="shownUsername"
          :readonly="!isEditingUsername"
          class="rounded-r-none w-2/3"
          @update:modelValue="newUsername = $event"
          @keydown.enter="changeUsername"
        />
        <FairButton class="w-1/3 rounded-l-none" @click="changeUsername"
          >{{ isEditingUsername ? lang("apply") : lang("edit") }}
        </FairButton>
      </div>
    </div>
    <template v-if="isGuest">
      <div
        class="flex flex-row w-full max-w-reader/2 justify-around gap-3 max-w-reader/2"
      >
        <FairButton class="w-full" @click="exportUuid">Export UUID</FairButton>
        <FairButton class="w-full" @click="importUuid">Import UUID</FairButton>
      </div>
      <FairButton
        class="w-full max-w-reader/2"
        @click="openUpgradeAccountDialog = true"
        >{{ lang("linkAccount.title") }}
      </FairButton>
      <UpgradeAccountDialog
        :open="openUpgradeAccountDialog"
        @close="openUpgradeAccountDialog = false"
      />
    </template>
    <template v-else>
      <div class="max-w-reader/2 -mt-5 w-full flex flex-col">
        <div>{{ lang("email") }}:</div>
        <div class="flex flex-row">
          <FairInput
            :class="{ 'pointer-events-none': !isEditingEmail }"
            :model-value="shownEmail"
            :readonly="!isEditingEmail"
            :type="isEditingEmail ? 'text' : 'password'"
            autocomplete="off"
            class="rounded-r-none w-2/3"
            @update:modelValue="newEmail = $event"
          />
          <FairButton class="w-1/3 rounded-l-none" @click="changeEmail"
            >{{ isEditingEmail ? lang("apply") : lang("edit") }}
          </FairButton>
          <ConfirmEmailChangeDialog
            :open="openConfirmEmailChangeDialog"
            @close="openConfirmEmailChangeDialog = false"
          />
        </div>
      </div>
      <FairButton
        class="max-w-reader/2 w-full"
        @click="openChangePasswordDialog = true"
        >{{ lang("changePassword.title") }}
      </FairButton>
      <ChangePasswordDialog
        :open="openChangePasswordDialog"
        @close="openChangePasswordDialog = false"
      />
    </template>
    <FairButton class="max-w-reader/2 w-full" @click="authStore.actions.logout"
      >{{ lang("logout") }}
    </FairButton>
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from "vue";
import Cookies from "js-cookie";
import { useSeoMeta } from "#head";
import { useAuthStore } from "~/store/authentication";
import FairButton from "~/components/interactables/FairButton.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import { useAccountStore } from "~/store/account";
import ConfirmEmailChangeDialog from "~/components/account/ConfirmEmailChangeDialog.vue";
import ChangePasswordDialog from "~/components/account/ChangePasswordDialog.vue";
import UpgradeAccountDialog from "~/components/account/UpgradeAccountDialog.vue";
import { useLang } from "~/composables/useLang";

useSeoMeta({
  title: "Account",
});

const lang = useLang("account");
const authStore = useAuthStore();
const accountStore = useAccountStore();

const isGuest = authStore.getters.isGuest;

const shownUsername = computed<string>(() => {
  if (isEditingUsername.value) return newUsername.value;
  return accountStore.state.username + "#" + accountStore.state.accountId;
});
const newUsername = ref<string>(accountStore.state.username);
const isEditingUsername = ref<boolean>(false);

const shownEmail = computed<string>(() => {
  if (isEditingEmail.value) return newEmail.value;
  return accountStore.state.email;
});
const newEmail = ref<string>(accountStore.state.username);
const isEditingEmail = ref<boolean>(false);

const openConfirmEmailChangeDialog = ref<boolean>(false);
const openChangePasswordDialog = ref<boolean>(false);
const openUpgradeAccountDialog = ref<boolean>(false);

function changeUsername() {
  if (!isEditingUsername.value) {
    isEditingUsername.value = true;
    newUsername.value = accountStore.state.username;
    return;
  }

  if (accountStore.state.username === newUsername.value) {
    isEditingUsername.value = false;
    return;
  }

  accountStore.actions
    .changeDisplayName(newUsername.value)
    .then(() => {
      isEditingUsername.value = false;
    })
    .catch(() => {
      isEditingUsername.value = false;
    });
}

function changeEmail() {
  if (!isEditingEmail.value) {
    isEditingEmail.value = true;
    newEmail.value = accountStore.state.email;
    return;
  }

  if (accountStore.state.email === newEmail.value) {
    isEditingEmail.value = false;
    return;
  }

  // openConfirmEmailChangeDialog.value = true;
  authStore.actions
    .changeEmail(newEmail.value)
    .then(() => {
      isEditingEmail.value = false;
      openConfirmEmailChangeDialog.value = true;
    })
    .catch(() => {});
}

async function exportUuid() {
  const uuid = Cookies.get("_uuid");
  if (uuid === undefined) {
    alert("You don't have a UUID");
    return;
  }
  await navigator.clipboard.writeText(uuid);
  alert("Copied your UUID to clipboard! (don't loose it or give it away)");
}

function importUuid() {
  const uuid = prompt(
    "Paste your ID into here (your old uuid will be copied into your clipboard):"
  );
  if (uuid === null || undefined) return;
  authStore.actions
    .login(uuid, uuid, true)
    .then(() => {
      alert("Successfully imported your UUID!");
      window.location.reload();
    })
    .catch(() => {
      alert("Failed to import your UUID!");
      window.location.reload();
    });
}

onMounted(() => {
  accountStore.actions.init().then();
});
</script>

<style lang="scss" scoped></style>
