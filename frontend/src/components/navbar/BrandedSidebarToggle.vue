<template>
  <div class="flex space-x-4 items-center justify-start min-w-min">
    <button
      aria-controls="mobile-menu"
      aria-expanded="false"
      class="inline-flex items-center justify-center rounded-md py-0.5 px-1 text-button-text hover:bg-button-bg-hover hover:text-button-text-hover"
      data-drawer-target="mobile-menu"
      type="button"
      @click="uiStore.toggleSidebar()"
    >
      <span class="sr-only">Open Main Menu</span>
      <Bars3Icon class="w-6 h-6" />
    </button>
    <div class="flex items-center min-w-fit">
      <div class="flex-shrink-0">
        <!--FairGame Logo and Branding with a link to the main page-->
        <NuxtLink
          :to="authStore.getters.homeLocation"
          class="select-none flex space-x-1"
          draggable="false"
        >
          <img
            alt="FairGame"
            class="w-7 h-7"
            draggable="false"
            src="/favicon.ico"
          />
          <span class="text-xl text-text"
            ><span class="text-text-light">Fair</span>Game</span
          >
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Bars3Icon } from "@heroicons/vue/24/outline";
import { useUiStore } from "~/store/ui";
import { useAuthStore } from "~/store/authentication";

const uiStore = useUiStore();
const authStore = await useAuthStore();
</script>

<style scoped></style>
