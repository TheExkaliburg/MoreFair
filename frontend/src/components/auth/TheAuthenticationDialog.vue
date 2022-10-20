<template>
  <Dialog :open="props.open" class="relative z-50">
    <div aria-hidden="true" class="fixed inset-0 bg-black/30" />
    <div class="fixed inset-0 flex items-center justify-center p-4">
      <DialogPanel
        v-if="isLogin"
        class="w-full max-w-sm rounded-3xl bg-pink-400 p-4"
      >
        <DialogTitle>Log in</DialogTitle>
        <DialogDescription>
          Don't have an account?
          <a class="text-blue-600" href="#" @click="isLogin = false">Sign up</a>
        </DialogDescription>
        <form class="flex flex-col h-full" @submit="login">
          <div class="h-10 py-3">E-Mail:</div>
          <input v-model="email" class="" name="email" type="text" />
          <div class="h-10 py-3">Password:</div>
          <input v-model="password" name="password" type="password" />
          <div class="h-10 py-3"></div>
          <button name="submit" type="submit" value="submit">Submit</button>
        </form>
      </DialogPanel>
      <DialogPanel v-else class="w-full max-w-sm rounded-3xl bg-pink-400 p-4">
        <DialogTitle>Sign up</DialogTitle>
        <DialogDescription>
          Already have an account?
          <a class="text-blue-600" href="#" @click="isLogin = true">Log in</a>
        </DialogDescription>
        <form class="flex flex-col h-full" @submit="signup">
          <div class="h-10 py-3">E-Mail:</div>
          <input v-model="email" class="" name="email" type="text" />
          <div class="h-10 py-3">Password:</div>
          <input
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            name="password"
          />
          <div v-if="!showPassword" class="h-10 py-3">Repeat Password:</div>
          <input
            v-if="!showPassword"
            v-model="repeatedPassword"
            name="repeatedPassword"
            type="password"
          />
          <div class="flex flex-row">
            <div class="flex flex-row justify-center">
              <div class="h-16 py-3 align-middle">Show Password:</div>
              <input
                v-model="showPassword"
                name="showPassword"
                type="checkbox"
              />
            </div>
            <div class="h-16 p-1">{{ strength.toString }}</div>
          </div>
          <button name="submit" type="submit" value="submit">Submit</button>
        </form>
      </DialogPanel>
    </div>
  </Dialog>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import {
  Dialog,
  DialogDescription,
  DialogPanel,
  DialogTitle,
} from "@headlessui/vue";
import { FeedbackType } from "@zxcvbn-ts/core/dist/types";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAPI } from "~/composables/useAPI";

const isLogin = ref<boolean>(true);

const props = defineProps({
  open: { type: Boolean, required: false, default: false },
});

const zxcvbn = useZxcvbn();
const api = useAPI();

const email = ref<string>("");
const password = ref<string>("");
const repeatedPassword = ref<string>("");
const showPassword = ref<boolean>(false);

const strength = computed<{
  score: number;
  feedback: FeedbackType;
  toString: string;
}>(() => {
  const result = zxcvbn(password.value);
  const { score, feedback } = result;

  return { score, feedback, toString: `Score: ${score}` };
});

function signup(event) {
  event.preventDefault();

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
  if (!showPassword.value && password.value !== repeatedPassword.value) {
    alert("Passwords do not match");
    return;
  }

  api.auth.register(email.value, password.value).then((response) => {
    if (response.status === 201) {
      alert(response.data);
    } else {
      alert("Error while registering");
    }
  });
}

function login(event) {
  event.preventDefault();
  api.auth.login(email.value, password.value).then((response) => {
    if (response.status === 200) {
      alert("Successfully logged in");
    } else {
      alert("Error while logging in");
    }
  });
}
</script>
