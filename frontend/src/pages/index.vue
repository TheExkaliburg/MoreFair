<template>
  <div
    class="flex flex-col justify-center items-center w-screen h-screen bg-navbar-bg"
  >
    <div class="text-5xl text-text">FairGame</div>
    <div class="flex flex-row justify-center content-center">
      <FairButton class="mx-1 my-3" @click="registerGuest"
        >Play as Guest
      </FairButton>
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
import { onMounted, ref } from "vue";
import { navigateTo } from "nuxt/app";
import FairButton from "../components/interactables/FairButton.vue";
import TheAuthenticationDialog from "../components/auth/TheAuthenticationDialog.vue";
import { useAuthStore } from "~/store/authentication";

const authStore = useAuthStore();
definePageMeta({ layout: "empty" });

const isLoginModalOpen = ref<boolean>(false);

onMounted(async () => {
  // If guest-uuid exists try logging in
  if (authStore.isGuest) {
    await authStore.login(authStore.state.uuid, authStore.state.uuid);
  }
  if (authStore.state.authenticationStatus) {
    await navigateTo("/game");
  }
});

function openLoginModal() {
  if (authStore.state.authenticationStatus) {
    return navigateTo("/game");
  }

  isLoginModalOpen.value = true;
}

async function registerGuest() {
  if (authStore.state.authenticationStatus) {
    await navigateTo("/game");
    return;
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
