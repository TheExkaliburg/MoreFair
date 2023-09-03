<template>
  <FairDialog
    v-if="isForgot"
    :class="{ 'cursor-wait': isWaiting }"
    :title="lang('title')"
    @close="close"
  >
    <template #description>
      {{ lang("description1") }}
      <a href="#" @click="isForgot = false">{{ lang("description1a") }}</a>
    </template>
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">{{ lang("email") }}:</div>
      <FairInput
        v-model="email"
        :disabled="isWaiting"
        autocomplete="email"
        name="email"
        required
        type="email"
      />
      <FairButton
        :disabled="!canSubmitEmail"
        class="mt-4 self-end"
        @click="submit"
        >{{ lang("submit") }}
      </FairButton>
    </form>
  </FairDialog>
  <FairDialog
    v-else
    :class="{ 'cursor-wait': isWaiting }"
    :description="lang('description2')"
    :title="lang('title')"
    click-outside-to-close="false"
    @close="close"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">{{ lang("token") }}:</div>
      <FairInput
        v-model="token"
        :disabled="isWaiting"
        autocomplete="off"
        name="token"
        required
        type="text"
      />
      <div class="pt-4">{{ lang("newPassword") }}:</div>
      <FairInput
        v-model="newPassword"
        :disabled="isWaiting"
        autocomplete="new-password"
        name="newPassword"
        required
        type="password"
      />
      <div class="pt-4">{{ lang("repeatPassword") }}:</div>
      <FairInput
        v-model="repeatPassword"
        :disabled="isWaiting"
        autocomplete="off"
        name="repeatPassword"
        required
        type="password"
      />
      <div>{{ strength }}</div>
      <FairButton
        :disabled="!canSubmitPasswords"
        class="mt-4 self-end"
        @click="changePassword"
        >{{ lang("submit") }}
      </FairButton>
    </form>
  </FairDialog>
</template>

<script lang="ts" setup>
import { watch } from "vue";
import { debounce } from "@zxcvbn-ts/core";
import FairDialog from "~/components/interactables/FairDialog.vue";
import { useLang } from "~/composables/useLang";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAuthStore } from "~/store/authentication";
import { useToasts } from "~/composables/useToasts";

const authStore = useAuthStore();

const lang = useLang("account.forgotPassword");
const isForgot = ref<boolean>(true);
const token = ref<string>("");
const email = ref<string>("");
const newPassword = ref<string>("");
const repeatPassword = ref<string>("");
const isWaiting = ref<boolean>(false);

const emits = defineEmits(["close"]);

const strength = ref<string>((await useZxcvbn("")).toString);
watch(
  () => newPassword.value,
  debounce(
    async (value) => {
      strength.value = (await useZxcvbn(value)).toString;
    },
    200,
    false,
  ),
);

const canSubmitPasswords = computed(() => {
  return (
    !isWaiting.value &&
    token.value.length > 0 &&
    newPassword.value.length > 0 &&
    repeatPassword.value.length > 0 &&
    newPassword.value === repeatPassword.value
  );
});

const canSubmitEmail = computed(() => {
  return email.value.length > 0 && !isWaiting.value;
});

function submit() {
  isWaiting.value = true;
  authStore.actions
    .forgotPassword(email.value)
    .then(() => {
      isForgot.value = false;
      isWaiting.value = false;
    })
    .catch(() => {
      isWaiting.value = false;
    });
}

function changePassword() {
  isWaiting.value = true;

  if (newPassword.value !== repeatPassword.value) {
    useToasts("Passwords do not match", { type: "error" });
    isWaiting.value = false;
    return;
  }

  authStore.actions
    .resetPassword(token.value, newPassword.value)
    .then(() => {
      isWaiting.value = false;
      useToasts("Password changed, please login with your new password");
      close();
    })
    .catch(() => {
      isWaiting.value = false;
    });
}

function close() {
  isForgot.value = true;
  token.value = "";
  email.value = "";
  newPassword.value = "";
  repeatPassword.value = "";
  isWaiting.value = false;

  emits("close");
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
