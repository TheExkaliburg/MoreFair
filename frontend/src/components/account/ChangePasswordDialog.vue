<template>
  <FairDialog
    description="Please enter your current and new password below:"
    title="Change your Password"
    @close="close"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">Current Password:</div>
      <FairInput
        v-model="currentPassword"
        autocomplete="current-password"
        name="currentPassword"
        required
        type="password"
      />
      <div class="pt-4">New Password:</div>
      <FairInput
        v-model="newPassword"
        autocomplete="new-password"
        name="newPassword"
        required
        type="password"
      />
      <div class="pt-4">Repeat Password:</div>
      <FairInput
        v-model="repeatPassword"
        autocomplete="off"
        name="repeatPassword"
        required
        type="password"
      />
      <div>{{ zxcvbn.toString }}</div>
      <FairButton
        :disabled="!canSubmitPasswords"
        class="mt-4 self-end"
        @click="submit"
        >Confirm
      </FairButton>
    </form>
  </FairDialog>
</template>

<script lang="ts" setup>
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAuthStore } from "~/store/authentication";

const emit = defineEmits(["close"]);

const currentPassword = ref<string>("");
const newPassword = ref<string>("");
const repeatPassword = ref<string>("");

const authStore = useAuthStore();

const canSubmitPasswords = computed<boolean>(() => {
  return (
    newPassword.value.length > 0 &&
    repeatPassword.value.length > 0 &&
    currentPassword.value.length > 0
  );
});

const zxcvbn = useZxcvbn(newPassword);

function submit() {
  if (newPassword.value !== repeatPassword.value) {
    alert("Passwords do not match");
    return;
  }

  authStore.actions
    .changePassword(currentPassword.value, newPassword.value)
    .then(() => close())
    .catch(() => {});
}

function close() {
  currentPassword.value = "";
  newPassword.value = "";
  repeatPassword.value = "";
  emit("close");
}
</script>

<style scoped></style>
