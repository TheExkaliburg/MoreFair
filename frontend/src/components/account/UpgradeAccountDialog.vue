<template>
  <FairDialog
    :class="{ 'cursor-wait': isSubmitting }"
    :description="lang('description')"
    :title="lang('title')"
    @close="close"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">{{ lang("email") }}:</div>
      <FairInput
        v-model="email"
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="isSubmitting"
        autocomplete="email"
        name="email"
        required
        type="text"
      />
      <div class="pt-4">{{ lang("password") }}:</div>
      <FairInput
        v-model="password"
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="isSubmitting"
        autocomplete="new-password"
        name="password"
        required
        type="password"
      />
      <div class="pt-4">{{ lang("repeatPassword") }}:</div>
      <FairInput
        v-model="repeatedPassword"
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="isSubmitting"
        autocomplete="off"
        name="repeatedPassword"
        required
        type="password"
      />
      <div>{{ strength }}</div>
      <FairButton
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="!canSubmit || isSubmitting"
        class="mt-4 self-end"
        @click="submit"
        >{{ lang("submit") }}
      </FairButton>
    </form>
  </FairDialog>
</template>

<script lang="ts" setup>
import { watch } from "vue";
import { debounce } from "@zxcvbn-ts/core";
import FairDialog from "@/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import FairButton from "~/components/interactables/FairButton.vue";
import { useAuthStore } from "~/store/authentication";
import { useToasts } from "~/composables/useToasts";

const emits = defineEmits(["close"]);

const authStore = useAuthStore();
const lang = useLang("account.linkAccount");

const email = ref<string>("");
const password = ref<string>("");
const repeatedPassword = ref<string>("");

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

const isSubmitting = ref<boolean>(false);

const canSubmit = computed<boolean>(() => {
  return (
    password.value.length > 0 &&
    repeatedPassword.value.length > 0 &&
    email.value.length > 0
  );
});

function submit() {
  if (password.value !== repeatedPassword.value) {
    useToasts("Passwords do not match", { type: "error" });
    return;
  }

  isSubmitting.value = true;
  authStore.actions
    .upgradeGuest(email.value, password.value)
    .then(() => {
      isSubmitting.value = false;
      close();
    })
    .catch(() => {
      isSubmitting.value = false;
    });
}

function close() {
  if (isSubmitting.value) return;
  email.value = "";
  password.value = "";
  repeatedPassword.value = "";
  emits("close");
}
</script>

<style scoped></style>
