<template>
  <div
    class="flex flex-col justify-center items-center w-screen h-screen bg-navbar-bg"
  >
    <div class="text-5xl text-text">FairGame</div>
    <div class="flex flex-row justify-center content-center">
      <FairButton @click="registerGuest">Play as Guest</FairButton>
      <FairButton @click="openLoginModal">Login</FairButton>
    </div>
    <div class="text-text">Guest-UUID: {{ accountStore.uuid }}</div>
    <div class="text-text">
      Logged in: {{ accountStore.authenticationStatus }}
    </div>
    <div class="text-text"></div>
    <TheAuthenticationDialog
      :open="isLoginModalOpen"
      @close="isLoginModalOpen = false"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from "vue";
import { useAccountStore } from "~/store/account";
import FairButton from "~/components/interactables/FairButton.vue";
import TheAuthenticationDialog from "~/components/auth/TheAuthenticationDialog.vue";

const accountStore = useAccountStore();
definePageMeta({ layout: false });

const isLoginModalOpen = ref<boolean>(false);

onMounted(() => {
  // If guest-uuid exists try logging in
  if (accountStore.isGuest) {
    accountStore.login(accountStore.uuid, accountStore.uuid);
  }
  if (accountStore.authenticationStatus) {
    return navigateTo("/game");
  }
});

async function openLoginModal() {
  if (accountStore.authenticationStatus) {
    return await navigateTo("/game");
  }

  isLoginModalOpen.value = true;
}

async function registerGuest() {
  if (accountStore.authenticationStatus) {
    await navigateTo("/game");
  }

  if (
    accountStore.isGuest ||
    confirm(
      "As guest your account cannot access all features a normal account could. " +
        "You are also more susceptible to malicious scripts.\n\n " +
        "Are you sure you want to continue? (You can also upgrade to a full account for free later on)"
    )
  ) {
    accountStore.registerGuest();
  }
}
</script>
