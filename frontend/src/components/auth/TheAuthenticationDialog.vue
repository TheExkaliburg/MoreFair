<template>
  <FairDialog
    v-if="isLogin"
    :class="{ 'cursor-wait': isWaiting }"
    :title="lang('login.title')"
    @close="close"
  >
    <template #description>
      {{ lang("login.noAccount") }}
      <a href="#" @click="isLogin = false">{{ lang("signup.title") }}</a>
    </template>
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">{{ lang("login.email") }}:</div>
      <FairInput
        v-model="email"
        :disabled="isWaiting"
        class=""
        name="email"
        type="text"
      />
      <div class="pt-4">{{ lang("login.password") }}:</div>
      <FairInput
        v-model="password"
        :disabled="isWaiting"
        name="password"
        type="password"
      />
      <div class="flex flex-row w-full pt-1 justify-between">
        <div class="flex flex-row gap-2">
          <label
            class="cursor-pointer select-none"
            for="rememberMe"
            @click="rememberMe = !rememberMe"
            >{{ lang("login.rememberMe") }}:</label
          >
          <FairCheckboxInput
            id="rememberMe"
            v-model="rememberMe"
            :disabled="isWaiting"
          />
        </div>
        <a href="#" @click="openForgotPassword = true"> Forgot Password?</a>
        <ForgotPasswordDialog
          :open="openForgotPassword"
          @close="openForgotPassword = false"
        />
      </div>
      <FairButton :disabled="isWaiting" class="mt-4 self-end" @click="login"
        >{{ lang("login.submit") }}
      </FairButton>
    </form>
  </FairDialog>
  <FairDialog
    v-else
    :class="{ 'cursor-wait': isWaiting }"
    :title="lang('signup.title')"
    @close="close"
  >
    <template #description
      >{{ lang("signup.gotAccount") }}
      <a href="#" @click="isLogin = true">{{
        lang("login.title")
      }}</a></template
    >
    <form class="flex flex-col h-full" @submit.prevent>
      <div class="pt-4">{{ lang("signup.email") }}:</div>
      <FairInput
        v-model="email"
        :disabled="isWaiting"
        class=""
        name="email"
        type="text"
      />
      <div class="pt-4">{{ lang("signup.password") }}:</div>
      <FairInput
        v-model="password"
        :disabled="isWaiting"
        :type="showPassword ? 'text' : 'password'"
        name="password"
      />
      <div v-if="!showPassword" class="h-10 pt-4">
        {{ lang("signup.repeatPassword") }}:
      </div>
      <FairInput
        v-if="!showPassword"
        v-model="repeatedPassword"
        :disabled="isWaiting"
        name="repeatedPassword"
        type="password"
      />
      <div class="flex flex-row pt-1">
        {{ strength }}
      </div>

      <div class="flex flex-row pt-4">
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
        <FairButton :disabled="isWaiting" class="self-end" @click="signup"
          >{{ lang("signup.submit") }}
        </FairButton>
      </div>
    </form>
  </FairDialog>
</template>

<script lang="ts" setup>
import { ref, watch } from "vue";
import { debounce } from "@zxcvbn-ts/core";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAuthStore } from "~/store/authentication";
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import FairCheckboxInput from "~/components/interactables/FairCheckboxInput.vue";
import { useLang } from "~/composables/useLang";
import { useToasts } from "~/composables/useToasts";
import ForgotPasswordDialog from "~/components/auth/ForgotPasswordDialog.vue";

const lang = useLang("account");
const isLogin = ref<boolean>(true);
const authStore = useAuthStore();

const email = ref<string>("");
const password = ref<string>("");
const repeatedPassword = ref<string>("");
const showPassword = ref<boolean>(false);
const rememberMe = ref<boolean>(false);
const isWaiting = ref<boolean>(false);
const openForgotPassword = ref<boolean>(false);

const strength = ref<string>((await useZxcvbn("")).toString);
watch(
  () => password.value,
  debounce(
    async (value) => {
      strength.value = (await useZxcvbn(value)).toString;
    },
    200,
    false
  )
);

const emit = defineEmits(["close"]);

function signup() {
  isWaiting.value = true;
  if (!showPassword.value && password.value !== repeatedPassword.value) {
    useToasts("Passwords do not match", { type: "error" });
    return;
  }

  authStore.actions
    .registerAccount(email.value, password.value)
    .then(() => {
      isWaiting.value = false;
      close();
    })
    .catch(() => {
      isWaiting.value = false;
    });
}

function login() {
  isWaiting.value = true;
  authStore.actions
    .login(email.value, password.value, rememberMe.value)
    .then(() => {
      isWaiting.value = false;
      close();
    })
    .catch(() => {
      isWaiting.value = false;
    });
}

function close() {
  isLogin.value = true;
  email.value = "";
  password.value = "";
  repeatedPassword.value = "";
  showPassword.value = false;

  emit("close");
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
