<template>
  <nav class="bg-navbar-bg">
    <div class="mx-auto px-4">
      <div class="flex h-10 items-center justify-between">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <!--FairGame Logo and Branding with a link to the main page-->
            <NuxtLink to="/" class="flex">
              <img src="/favicon.ico" alt="FairGame" class="w-7 h-7" />
              <span class="text-xl text-navbar-text">FairGame</span>
            </NuxtLink>
          </div>
        </div>
        <div class="-mr-2 flex lg:hidden">
          <button
            type="button"
            class="inline-flex items-center justify-center rounded-md py-0.5 px-1 text-button-text hover:bg-button-bg-hover hover:text-button-text-hover"
            data-drawer-target="mobile-menu"
            aria-controls="mobile-menu"
            aria-expanded="false"
            @click="uiStore.sidebarExpanded = !uiStore.sidebarExpanded"
          >
            <span class="sr-only">Open Main Menu</span>
            <Bars3Icon class="w-6 h-6" />
          </button>
        </div>
      </div>
    </div>

    <OffCanvasBrandedSidebar />
  </nav>
</template>

<script setup lang="ts">
import { Bars3Icon } from "@heroicons/vue/24/outline";
import OffCanvasBrandedSidebar from "~/components/navbar/OffCanvasSidebar.vue";
import { useUiStore } from "~/store/ui";

const uiStore = useUiStore();
</script>

<style scoped lang="scss"></style>
