<template>
  <Dialog :open="true" class="relative z-50">
    <div class="fixed inset-0 bg-black/30" aria-hidden="true" />
    <div class="fixed inset-0 flex items-center justify-center p-4">
      <DialogPanel
        v-if="isLogin"
        class="w-full max-w-sm rounded-3xl bg-pink-400 p-4"
      >
        <DialogTitle>Log in</DialogTitle>
        <DialogDescription>
          Don't have an account?
          <a href="#" class="text-blue-600" @click="isLogin = false">Sign up</a>
        </DialogDescription>
        <form class="flex flex-col h-full" @submit="login">
          <div class="h-10 py-3">E-Mail:</div>
          <input v-model="email" type="text" name="email" class="" />
          <div class="h-10 py-3">Password:</div>
          <input v-model="password" type="password" name="password" />
          <div class="h-10 py-3"></div>
          <button type="submit" name="submit" value="submit">Submit</button>
        </form>
      </DialogPanel>
      <DialogPanel v-else class="w-full max-w-sm rounded-3xl bg-pink-400 p-4">
        <DialogTitle>Sign up</DialogTitle>
        <DialogDescription>
          Already have an account?
          <a href="#" class="text-blue-600" @click="isLogin = true">Log in</a>
        </DialogDescription>
        <form class="flex flex-col h-full" @submit="signup">
          <div class="h-10 py-3">E-Mail:</div>
          <input v-model="email" type="text" name="email" class="" />
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
            type="password"
            name="repeatedPassword"
          />
          <div class="flex flex-row">
            <div class="flex flex-row justify-center">
              <div class="h-10 py-3">Show Password:</div>
              <input
                v-model="showPassword"
                type="checkbox"
                name="showPassword"
              />
            </div>
            <div class="h-10 p-1">{{ strength.toString }}</div>
          </div>
          <button type="submit" name="submit" value="submit">Submit</button>
        </form>
      </DialogPanel>
    </div>
  </Dialog>

  <!--div class="bg-yellow-500 flex justify-center items-center">
    <form class="flex flex-col h-10" @submit="submit">
      <div class="h-10 p-1">Username:</div>
      <input v-model="username" type="text" name="email" class="" />
      <div class="h-10 p-1">Password:</div>
      <input v-model="password" type="password" name="password" />
      <div class="h-10 p-1">{{ strength.toString }}</div>
      <button type="submit" name="submit" value="submit">Submit</button>
    </form>
  </div-->
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import {
  Dialog,
  DialogDescription,
  DialogPanel,
  DialogTitle,
} from "@headlessui/vue";
import { FeedbackType } from "@zxcvbn-ts/core/dist/types";
import axios from "axios";
import { useZxcvbn } from "~/composables/useZxcvbn";

const isLogin = ref<boolean>(true);

const zxcvbn = useZxcvbn();

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

  axios
    .post("http://localhost:8080/api/auth/signup", {
      params: {
        email: email.value,
        password: password.value,
      },
    })
    .then((response) => {
      console.log(response);
    })
    .catch((error) => {
      console.log(error);
    });
}
function login(event) {
  event.preventDefault();
  axios
    .post("http://localhost:8080/api/auth/login", {
      email: email.value,
      password: password.value,
    })
    .then((response) => {
      console.log(response);
    })
    .catch((error) => {
      console.log(error);
    });
}
</script>
