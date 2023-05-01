<template>
  <FairDialog
    title="Change your Password"
    description="Please enter your current and new password below:"
    @close="$emit('close')"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">Current Password:</div>
      <FairInput
        v-model="currentPassword"
        type="password"
        name="currentPassword"
        required
        autocomplete="current-password"
      />
      <div class="pt-4">New Password:</div>
      <FairInput
        v-model="newPassword"
        type="password"
        name="newPassword"
        required
        autocomplete="new-password"
      />
      <div class="pt-4">Repeat Password:</div>
      <FairInput
        v-model="repeatPassword"
        type="password"
        name="repeatPassword"
        required
        autocomplete="off"
      />
      <div>{{ strength.toString }}</div>
      <FairButton class="mt-4 self-end" @click="submit">Confirm</FairButton>
    </form>
  </FairDialog>
</template>

<script setup lang="ts">
import { FeedbackType } from "@zxcvbn-ts/core/dist/types";
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";

const api = useAPI();

const emit = defineEmits(["close"]);

const currentPassword = ref<string>("");
const newPassword = ref<string>("");
const repeatPassword = ref<string>("");

const zxcvbn = useZxcvbn();

const strength = computed<{
  score: number;
  feedback: FeedbackType;
  toString: string;
}>(() => {
  const result = zxcvbn(newPassword.value);
  const { score, feedback } = result;
  return { score, feedback, toString: `Score: ${score}` };
});

function submit() {
  if (newPassword.value.length > 64) {
    alert("Password can only be 64 characters long");
    return;
  }
  if (newPassword.value.length < 8) {
    alert("Password needs to be at least 8 characters long");
    return;
  }
  if (strength.value.score < 3) {
    alert(
      `Password is too weak.\n\n${strength.value.feedback.warning}\n${strength.value.feedback.suggestions}`
    );
  }
  if (newPassword.value !== repeatPassword.value) {
    alert("Passwords do not match");
  }

  api.auth
    .changePassword(currentPassword.value, newPassword.value)
    .then((res) => {
      alert(res.data);
      emit("close");
    });
}
</script>

<style scoped></style>
