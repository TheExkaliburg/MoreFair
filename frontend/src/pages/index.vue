<template>
  <div
    class="flex flex-col justify-center items-center w-screen h-screen bg-navbar-bg"
  >
    <div class="text-5xl color-text-color">FairGame</div>
    <div class="flex flex-row justify-center content-center">
      <FairButton>Play as Guest</FairButton>
      <FairButton>Login</FairButton>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useAccountStore } from "~/store/account";
import FairButton from "~/components/interactables/FairButton.vue";

const accountStore = useAccountStore();
definePageMeta({ layout: false });

onBeforeMount(() => {
  accountStore.login();
  if (accountStore.isLoggedIn()) {
    // await navigateTo("/game");
  }
});
</script>
