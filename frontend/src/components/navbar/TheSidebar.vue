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
            to="/account"
          >
            <SidebarButton>
              <template #icon>
                <UserCircleIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('options'), placement: 'right' }"
            to="/options"
          >
            <SidebarButton>
              <template #icon>
                <Cog8ToothIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('help'), placement: 'right' }"
            @click="help"
          >
            <SidebarButton>
              <template #icon>
                <QuestionMarkCircleIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('rules'), placement: 'right' }"
            to="/rules"
          >
            <SidebarButton>
              <template #icon>
                <DocumentTextIcon />
              </template>
            </SidebarButton>
          </NuxtLink>
          <NuxtLink
            v-tippy="{ content: lang('discord'), placement: 'right' }"
            target="_blank"
            to="https://discord.gg/ThKzCknfFr"
          >
            <SidebarButton>
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
  Cog8ToothIcon,
  DocumentTextIcon,
  QuestionMarkCircleIcon,
  UserCircleIcon,
} from "@heroicons/vue/24/outline";
import { useLang } from "../../composables/useLang";
import SidebarButton from "~/components/navbar/SidebarButton.vue";
import { useTutorialTour } from "~/composables/useTour";

const classesForSize = "min-w-12 w-12 max-w-12 px-2 py-2";
const lang = useLang("components.navbar.sidebar");

function help() {
  useTutorialTour().start();
}
</script>

<style lang="scss" scoped></style>
