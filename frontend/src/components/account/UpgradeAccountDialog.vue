<template>
  <FairDialog
    :class="{ 'cursor-wait': isSubmitting }"
    description="Please enter your email and your password below to upgrade from a guest account."
    title="Upgrade Guest Account"
    @close="close"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">E-Mail:</div>
      <FairInput
        v-model="email"
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="isSubmitting"
        autocomplete="email"
        name="email"
        required
        type="text"
      />
      <div class="pt-4">Password:</div>
      <FairInput
        v-model="password"
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="isSubmitting"
        autocomplete="new-password"
        name="password"
        required
        type="password"
      />
      <div class="pt-4">Repeat Password:</div>
      <FairInput
        v-model="repeatedPassword"
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="isSubmitting"
        autocomplete="off"
        name="repeatedPassword"
        required
        type="password"
      />
      <div>{{ zxcvbn.toString }}</div>
      <FairButton
        :class="{ 'cursor-wait': isSubmitting }"
        :disabled="!canSubmit || isSubmitting"
        class="mt-4 self-end"
        @click="submit"
        >Confirm
      </FairButton>
    </form>
  </FairDialog>
</template>

<script lang="ts" setup>
import FairDialog from "@/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import FairButton from "~/components/interactables/FairButton.vue";
import { useAuthStore } from "~/store/authentication";

const emits = defineEmits(["close"]);

const authStore = useAuthStore();

const email = ref<string>("");
const password = ref<string>("");
const repeatedPassword = ref<string>("");
const zxcvbn = useZxcvbn(password);

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
    alert("Passwords do not match");
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
