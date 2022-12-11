<template>
  <div
    class="flex flex-col justify-center items-center w-screen h-screen bg-navbar-bg"
  >
    <div class="text-5xl text-text">FairGame</div>
    <div class="flex flex-row justify-center content-center">
      <FairButton class="mx-1 my-3" @click="registerGuest"
        >Play as Guest</FairButton
      >
      <FairButton class="mx-1 my-3" @click="openLoginModal">Login</FairButton>
    </div>
    <div class="text-text">Guest-UUID: {{ authStore.uuid }}</div>
    <div class="text-text">Logged in: {{ authStore.authenticationStatus }}</div>
    <div class="text-text"></div>
    <TheAuthenticationDialog
      :open="isLoginModalOpen"
      @close="isLoginModalOpen = false"
    />
  </div>
</template>

<script lang="ts" setup>
import { onBeforeMount } from "vue";
import { useAuthStore } from "~/store/authentication";
import FairButton from "~/components/interactables/FairButton.vue";
import TheAuthenticationDialog from "~/components/auth/TheAuthenticationDialog.vue";

const authStore = useAuthStore();
definePageMeta({ layout: "empty" });

const isLoginModalOpen = ref<boolean>(false);

onBeforeMount(async () => {
  // If guest-uuid exists try logging in
  if (authStore.isGuest) {
    await authStore.login(authStore.uuid, authStore.uuid);
  }
  if (authStore.authenticationStatus) {
    return await navigateTo("/game");
  }
});

function openLoginModal() {
  if (authStore.authenticationStatus) {
    return navigateTo("/game");
  }

  isLoginModalOpen.value = true;
}

async function registerGuest() {
  if (authStore.authenticationStatus) {
    return await navigateTo("/game");
  }

  if (
    authStore.isGuest ||
    confirm(
      "As guest your account cannot access all features a normal account could. " +
        "You are also more susceptible to malicious scripts.\n\n " +
        "Are you sure you want to continue? (You can also upgrade to a full account for free later on)"
    )
  ) {
    await authStore.registerGuest();
  }
}
</script>
