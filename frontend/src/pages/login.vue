<template>
  <div
    class="flex flex-col justify-center items-center w-screen h-screen bg-background"
  >
    <div
      class="flex flex-col justify-center items-center h-content py-16 w-full bg-navbar-bg relative border-text-dark border-y-1"
    >
      <img
        alt="FairGame"
        class="h-32 absolute -top-16"
        draggable="false"
        src="/img/ladder.png"
      />
      <div class="pt-10 text-5xl text-text">
        <span class="text-text-light">Fair</span>Game
      </div>
      <div class="flex flex-col justify-center content-center gap-3 pt-12 w-56">
        <FairButton
          :disabled="isWaiting"
          class="w-full h-14 font-bold text-lg rounded-xl"
          @click="openLoginModal"
          >Login
        </FairButton>
        <FairButton
          :disabled="isWaiting"
          class="w-full h-14 font-bold text-lg rounded-xl"
          @click="isGuestDialogOpen = true"
          >Play as Guest
        </FairButton>
        <FairDialog
          :class="{ 'cursor-wait': isWaiting }"
          :open="isGuestDialogOpen"
          title="Play as Guest"
          @close="isGuestDialogOpen = false"
        >
          <template #description>
            <p>
              As a guest your account cannot access all the features a normal
              account could. You are also more susceptible to malicious scripts
              or losing your progress.
            </p>
            <br />
            <p>
              Are you sure you want to continue? (You can link your account to
              an email later on)
            </p>
            <br />
            <p>
              {{ lang("signup.agreeTo") }}
              <NuxtLink target="_blank" to="/rules"
                >{{ lang("signup.rules") }}
              </NuxtLink>
              {{ lang("signup.agreeTo2") }}
              <NuxtLink class="whitespace-nowrap" target="_blank" to="/privacy"
                >{{ lang("signup.privacy") }}.
              </NuxtLink>
            </p>
          </template>
          <div class="flex flex-row justify-between pt-2">
            <FairButton :disabled="isWaiting" @click="importUuid"
              >Import UUID
            </FairButton>
            <FairButton :disabled="isWaiting" @click="registerGuest"
              >Confirm
            </FairButton>
          </div>
        </FairDialog>
      </div>
      <div v-if="false" class="text-text">
        Logged in: {{ authStore.state.authenticationStatus }} Guest:
        {{ authStore.getters.isGuest }}
      </div>
      <div class="text-text"></div>
      <TheAuthenticationDialog
        :open="isLoginModalOpen"
        @close="isLoginModalOpen = false"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { navigateTo } from "nuxt/app";
import { useSeoMeta } from "#head";
import FairButton from "../components/interactables/FairButton.vue";
import TheAuthenticationDialog from "../components/auth/TheAuthenticationDialog.vue";
import { useAuthStore } from "~/store/authentication";
import FairDialog from "~/components/interactables/FairDialog.vue";
import { useLang } from "~/composables/useLang";
import { useToasts } from "~/composables/useToasts";

const lang = useLang("account");
const authStore = useAuthStore();
definePageMeta({ layout: "empty" });

useSeoMeta({
  title: "FairGame",
});

const isLoginModalOpen = ref<boolean>(false);
const isGuestDialogOpen = ref<boolean>(false);
const isWaiting = ref<boolean>(false);

function openLoginModal() {
  if (authStore.state.authenticationStatus) {
    return navigateTo("/");
  }

  isLoginModalOpen.value = true;
}

function importUuid() {
  const uuid = prompt("Paste your UUID into here:");
  if (uuid === null || undefined) return;
  authStore.actions
    .login(uuid, uuid, true)
    .then(() => {
      authStore.state.uuid = uuid;
      useToasts("Successfully imported your UUID!");
    })
    .catch(() => {
      useToasts("Failed to import your UUID!", { type: "error" });
    });
}

async function registerGuest() {
  if (authStore.state.authenticationStatus) {
    await navigateTo("/");
    return;
  }

  isWaiting.value = true;

  await authStore.actions
    .registerGuest()
    .then(() => {
      navigateTo("/");
    })
    .catch(() => {
      isGuestDialogOpen.value = false;
      isWaiting.value = false;
    });
}
</script>

<style lang="scss" scoped>
a {
  color: var(--link-color);

  &:hover {
    color: var(--link-hover-color);
  }
}
</style>
