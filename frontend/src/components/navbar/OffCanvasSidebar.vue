<template>
  <div
    ref="offCanvas"
    :class="{
      '-translate-x-full backdrop-blur-xl': !uiStore.sidebarExpanded,
    }"
    class="bg-navbar-bg fixed inset-y-0 left-0 pt-1.5 w-1/10 min-w-max max-w-full text-navbar-text px-2 py-1 transform transition-transform z-20 overflow-y-scroll hide-scrollbar"
    tabindex="-1"
  >
    <div class="h-full flex flex-col justify-between">
      <!--Top of the Canvas-->
      <div class="min-w-min flex flex-col justify-items-start content-around">
        <BrandedSidebarToggle />
        <!--Leaving this spot free-->
        <SidebarButton class="cursor-auto" />
      </div>
      <!--Bottom of the Canvas-->
      <div class="min-w-min flex flex-col justify-items-start content-around">
        <NuxtLink to="/">
          <SidebarButton :label="optionsLabel">
            <template #icon>
              <Cog8ToothIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink to="/">
          <SidebarButton :label="helpLabel">
            <template #icon>
              <QuestionMarkCircleIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink to="/">
          <SidebarButton :label="discordLabel">
            <template #icon>
              <font-awesome-icon icon="fa-brands fa-discord" />
            </template>
          </SidebarButton>
        </NuxtLink>
      </div>
    </div>
    <div class="flex flex-row space-x-4 justify-center text-white">
      <NuxtLink to="/impressum">{{ impressumLabel }}</NuxtLink>
      <NuxtLink to="/privacy">{{ privacyLabel }}</NuxtLink>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {
  Cog8ToothIcon,
  QuestionMarkCircleIcon,
} from "@heroicons/vue/24/outline";

import { computed, ref } from "vue";
import { useUiStore } from "~/store/ui";
import SidebarButton from "~/components/navbar/SidebarButton.vue";
import { onClickOutside, useLang } from "#imports";
import BrandedSidebarToggle from "~/components/navbar/BrandedSidebarToggle.vue";

const uiStore = useUiStore();
const offCanvas = ref<HTMLElement | null>(null);

const lang = useLang("components.navbar.offcanvas-sidebar");
const optionsLabel = computed<string>(() => lang("options"));
const helpLabel = computed<string>(() => lang("help"));
const discordLabel = computed<string>(() => lang("discord"));
const privacyLabel = computed<string>(() => lang("privacy"));
const impressumLabel = computed<string>(() => lang("impressum"));

uiStore.sidebarExpanded = false;

onClickOutside(offCanvas, () => {
  uiStore.sidebarExpanded = false;
});
</script>

<style lang="scss" scoped>
.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
}
</style>
