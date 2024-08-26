<template>
  <div class="flex flex-row">
    <div :class="classesForSize" class="-sm:hidden bg-navbar-bg shrink-0">
      <div
        :class="classesForSize"
        class="flex flex-col justify-between fixed left-0 inset-y-0 pt-10 justify-between shrink-0 bg-navbar-bg"
      >
        <!--Top of the Canvas-->
        <div class="min-w-min flex flex-col justify-items-start content-around">
          <!--Leaving this spot free-->
          <!--SidebarButton class="cursor-auto" /-->
        </div>
        <!--Bottom of the Canvas-->
        <div class="min-w-min flex flex-col justify-items-start content-around">
          <NuxtLink
            v-tippy="{ content: lang('account'), placement: 'right' }"
            aria-label="Goto Account Page"
            to="/account"
          >
            <SidebarButton aria-label="Goto Account Page">
              <template #icon>
                <UserCircleIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('options'), placement: 'right' }"
            aria-label="Goto Options Page"
            to="/options"
          >
            <SidebarButton aria-label="Goto Options Page">
              <template #icon>
                <Cog8ToothIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <SidebarButton
            v-tippy="{ content: lang('help'), placement: 'right' }"
            aria-label="Start Tutorial"
            @click="help"
          >
            <template #icon>
              <QuestionMarkCircleIcon />
            </template>
          </SidebarButton>
          <NuxtLink
            v-tippy="{ content: lang('wiki'), placement: 'right' }"
            aria-label="Goto Fair Wiki"
            target="_blank"
            to="https://fairwiki.kaliburg.de/"
          >
            <SidebarButton aria-label="Goto Fair Wiki">
              <template #icon>
                <BookOpenIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('changelog'), placement: 'right' }"
            aria-label="Goto Changelog"
            to="/changelog"
          >
            <SidebarButton aria-label="Goto Changelog">
              <template #icon>
                <NewspaperIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('rules'), placement: 'right' }"
            aria-label="Goto Rules Page"
            to="/rules"
          >
            <SidebarButton aria-label="Goto Rules Page">
              <template #icon>
                <DocumentTextIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-if="accountStore.getters.isMod"
            v-tippy="{ content: lang('moderation'), placement: 'right' }"
            aria-label="Goto Moderation Page"
            to="/moderation"
          >
            <SidebarButton aria-label="Goto Moderation Page">
              <template #icon>
                <font-awesome-icon icon="fa-solid fa-shield-halved" />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('discord'), placement: 'right' }"
            aria-label="Goto Community Discord"
            target="_blank"
            to="https://discord.gg/ThKzCknfFr"
          >
            <SidebarButton aria-label="Goto Community Discord">
              <template #icon>
                <font-awesome-icon icon="fa-brands fa-discord" />
              </template>
            </SidebarButton>
          </NuxtLink>
        </div>
      </div>
    </div>
    <slot />
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
import { useLang } from "~/composables/useLang";
import SidebarButton from "~/components/navbar/SidebarButton.vue";
import { useStartupTour } from "~/composables/useTour";
import { useAccountStore } from "~/store/account";

const accountStore = useAccountStore();

const classesForSize = "min-w-12 w-12 max-w-12 px-2 py-2";
const lang = useLang("components.navbar.sidebar");

function help() {
  useStartupTour().start();
}
</script>

<style lang="scss" scoped></style>
