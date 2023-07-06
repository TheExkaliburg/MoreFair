<template>
  <div
    ref="offCanvas"
    :class="{
      '-translate-x-full backdrop-blur-xl': !uiStore.state.sidebarExpanded,
    }"
    class="bg-navbar-bg fixed inset-y-0 left-0 pt-1.5 w-1/10 min-w-max max-w-full text-navbar-text px-2 py-1 transform transition-transform z-20 overflow-y-scroll hide-scrollbar"
    tabindex="-1"
  >
    <div class="h-full flex flex-col justify-between">
      <!--Top of the Canvas-->
      <div class="min-w-min flex flex-col justify-items-start content-around">
        <BrandedSidebarToggle />
        <!--Leaving this spot free-->
        <!--SidebarButton class="cursor-auto" /-->
      </div>
      <!--Bottom of the Canvas-->
      <div class="min-w-min flex flex-col justify-items-start content-around">
        <NuxtLink aria-label="Goto Account Page" to="/account" @click="close">
          <SidebarButton
            :label="lang('account')"
            aria-label="Goto Account Page"
          >
            <template #icon>
              <UserCircleIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink aria-label="Goto Options Page" to="/options" @click="close">
          <SidebarButton :label="optionsLabel" aria-label="Goto Options Page">
            <template #icon>
              <Cog8ToothIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <SidebarButton
          :label="helpLabel"
          aria-label="Start Tutorial"
          data-tutorial="help"
          @click="help"
        >
          <template #icon>
            <QuestionMarkCircleIcon />
          </template>
        </SidebarButton>
        <NuxtLink
          to="https://fairwiki.kaliburg.de/"
          target="_blank"
          @click="close"
        >
          <SidebarButton :label="lang('wiki')" aria-label="Goto Fair Wiki">
            <template #icon>
              <BookOpenIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink to="/changelog" @click="close">
          <SidebarButton
            :label="lang('changelog')"
            aria-label="Goto Changelog Page"
          >
            <template #icon>
              <NewspaperIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink to="/rules" @click="close">
          <SidebarButton :label="lang('rules')" aria-label="Goto Rules Page">
            <template #icon>
              <DocumentTextIcon />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink
          v-if="accountStore.getters.isMod"
          to="/moderation"
          @click="close"
        >
          <SidebarButton
            :label="lang('moderation')"
            aria-label="Goto Moderation Page"
          >
            <template #icon>
              <font-awesome-icon icon="fa-solid fa-shield-halved" />
            </template>
          </SidebarButton>
        </NuxtLink>
        <NuxtLink
          aria-label="Goto Community Discord"
          target="_blank"
          to="https://discord.gg/ThKzCknfFr"
          @click="close"
        >
          <SidebarButton
            :label="discordLabel"
            aria-label="Goto Community Discord"
          >
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
  BookOpenIcon,
  Cog8ToothIcon,
  DocumentTextIcon,
  NewspaperIcon,
  QuestionMarkCircleIcon,
  UserCircleIcon,
} from "@heroicons/vue/24/outline";
import { MaybeElement, onClickOutside } from "@vueuse/core";
import SidebarButton from "../../components/navbar/SidebarButton.vue";
import BrandedSidebarToggle from "../../components/navbar/BrandedSidebarToggle.vue";
import { NuxtLink } from "#components";
import { useUiStore } from "~/store/ui";
import { useTutorialTour } from "~/composables/useTour";
import { useLang } from "~/composables/useLang";
import { useAccountStore } from "~/store/account";

const accountStore = useAccountStore();
const uiStore = useUiStore();
const offCanvas = ref<MaybeElement>();

const lang = useLang("components.navbar.sidebar");
const optionsLabel = computed<string>(() => lang("options"));
const helpLabel = computed<string>(() => lang("help"));
const discordLabel = computed<string>(() => lang("discord"));
const privacyLabel = computed<string>(() => lang("privacy"));
const impressumLabel = computed<string>(() => lang("impressum"));

onClickOutside(offCanvas.value, () => {
  close();
});

function close() {
  uiStore.state.sidebarExpanded = false;
}

function help() {
  close();
  useTutorialTour().start();
  uiStore.state.sidebarExpanded = false;
}
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
