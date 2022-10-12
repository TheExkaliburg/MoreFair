<template>
  <div
    class="flex flex-col justify-center items-center w-screen h-screen bg-navbar-bg"
  >
    <div class="text-5xl text-text">FairGame</div>
    <div class="flex flex-row justify-center content-center">
      <FairButton>Play as Guest</FairButton>
      <FairButton>Login</FairButton>
    </div>
    <div class="text-text">{{ accountStore.uuid }}</div>
    <div class="text-text">{{ accountStore.accessToken }}</div>
    <div class="text-text">{{ accountStore.refreshToken }}</div>
  </div>
</template>

<script lang="ts" setup>
import { useAccountStore } from "~/store/account";
import FairButton from "~/components/interactables/FairButton.vue";

const accountStore = useAccountStore();
definePageMeta({ layout: false });

onBeforeMount(() => {
  // If guest-uuid exists try logging in
  // If not try to refresh existing tokens as pseudo-login
  if (accountStore.uuid !== "") {
    accountStore.login(accountStore.uuid, accountStore.uuid);
  } else if (accountStore.refreshToken !== "") {
    accountStore.refresh();
  }
});
</script>
