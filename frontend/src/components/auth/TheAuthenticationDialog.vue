<template>
  <Dialog :open="props.open" class="relative z-50" @close="$emit('close')">
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
              <div class="h-10 p-2 align-middle">Show Password:</div>
              <input
                v-model="showPassword"
                class="p-2"
                name="showPassword"
                type="checkbox"
              />

              <div class="h-10 p-2">{{ zxcvbn.toString }}</div>
            </div>
          </div>
          <button name="submit" type="submit" value="submit">Submit</button>
        </form>
      </DialogPanel>
    </div>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import {
  Dialog,
  DialogDescription,
  DialogPanel,
  DialogTitle,
} from "@headlessui/vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAuthStore } from "~/store/authentication";

const isLogin = ref<boolean>(true);
const authStore = useAuthStore();

const props = defineProps({
  open: { type: Boolean, required: false, default: false },
});

const email = ref<string>("");
const password = ref<string>("");
const repeatedPassword = ref<string>("");
const showPassword = ref<boolean>(false);

const zxcvbn = useZxcvbn(password);
const emit = defineEmits(["close"]);

function signup(event: Event) {
  event.preventDefault();
  if (!showPassword.value && password.value !== repeatedPassword.value) {
    alert("Passwords do not match");
    return;
  }

  authStore.actions.registerAccount(email.value, password.value).then(() => {
    emit("close");
  });
}

function login(event: Event) {
  event.preventDefault();
  authStore.login(email.value, password.value).then(() => {
    emit("close");
  });
}
</script>
