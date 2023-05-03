<template>
  <FairDialog
    title="Upgrade Guest Account"
    description="Please enter your email and your password below to upgrade from a guest account."
    @close="$emit('close')"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">E-Mail:</div>
      <FairInput
        v-model="email"
        type="text"
        name="email"
        required
        autocomplete="email"
      />
      <div class="pt-4">Password:</div>
      <FairInput
        v-model="password"
        type="password"
        name="password"
        required
        autocomplete="new-password"
      />
      <div class="pt-4">Repeat Password:</div>
      <FairInput
        v-model="repeatedPassword"
        type="password"
        name="repeatedPassword"
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
import FairDialog from "@/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import FairButton from "~/components/interactables/FairButton.vue";
import { useAPI } from "~/composables/useAPI";
import { useAuthStore } from "~/store/authentication";

const emits = defineEmits(["close"]);

const authStore = useAuthStore();

const email = ref<string>("");
const password = ref<string>("");
const repeatedPassword = ref<string>("");
const zxcvbn = useZxcvbn();
const api = useAPI();

const strength = computed<{
  score: number;
  feedback: FeedbackType;
  toString: string;
}>(() => {
  const result = zxcvbn(password.value);
  const { score, feedback } = result;
  return { score, feedback, toString: `Score: ${score}` };
});

function submit() {
  // Check with regex if email is valid
  if (
    !email.value.match(
      /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,15}$/
    )
  ) {
    alert("Invalid email");
    return;
  }

  if (password.value.length > 64) {
    alert("Password can only be 64 characters long");
    return;
  }
  if (password.value.length < 8) {
    alert("Password needs to be at least 8 characters long");
    return;
  }
  if (strength.value.score < 3) {
    alert(
      `Password is too weak.\n\n${strength.value.feedback.warning}\n${strength.value.feedback.suggestions}`
    );
    return;
  }
  if (password.value !== repeatedPassword.value) {
    alert("Passwords do not match");
    return;
  }

  api.auth
    .register(email.value, password.value, authStore.state.uuid)
    .then((response) => {
      if (response.status === 201) {
        alert(response.data);
        emits("close");
      } else {
        alert("Error while registering");
      }
    });
}
</script>

<style scoped></style>
