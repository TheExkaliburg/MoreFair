<template>
  <FairDialog
    :description="lang('description') + ':'"
    :title="lang('title')"
    @close="close"
  >
    <form class="flex flex-col items-start" @submit.prevent>
      <div class="pt-4">{{ lang("currentPassword") }}:</div>
      <FairInput
        v-model="currentPassword"
        autocomplete="current-password"
        name="currentPassword"
        required
        type="password"
      />
      <div class="pt-4">{{ lang("newPassword") }}:</div>
      <FairInput
        v-model="newPassword"
        autocomplete="new-password"
        name="newPassword"
        required
        type="password"
      />
      <div class="pt-4">{{ lang("repeatPassword") }}:</div>
      <FairInput
        v-model="repeatPassword"
        autocomplete="off"
        name="repeatPassword"
        required
        type="password"
      />
      <div>{{ strength }}</div>
      <FairButton
        :disabled="!canSubmitPasswords"
        class="mt-4 self-end"
        @click="submit"
        >{{ lang("submit") }}
      </FairButton>
    </form>
  </FairDialog>
</template>

<script lang="ts" setup>
import { debounce } from "@zxcvbn-ts/core";
import { watch } from "vue";
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAuthStore } from "~/store/authentication";
import { useLang } from "~/composables/useLang";
import { useToasts } from "~/composables/useToasts";

const lang = useLang("account.changePassword");
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

const strength = ref<string>((await useZxcvbn("")).toString);
watch(
  () => newPassword.value,
  debounce(
    async (value) => {
      strength.value = (await useZxcvbn(value)).toString;
    },
    200,
    false
  )
);

function submit() {
  if (newPassword.value !== repeatPassword.value) {
    useToasts("Passwords do not match", { type: "error" });
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
