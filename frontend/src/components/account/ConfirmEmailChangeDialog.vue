<template>
  <FairDialog
    :click-outside-to-close="false"
    description="Please go to your email-inbox and paste the token that was sent to you in the input below:"
    title="Confirm Your Email"
    @close="close"
  >
    <div class="flex flex-col items-center">
      <div class="h-4" />
      <FairInput :model-value="token" @update:model-value="token = $event" />
      <div class="h-4" />
      <FairButton
        :disabled="token.length === 0"
        class="self-end"
        @click="confirmEmail"
        >Confirm
      </FairButton>
    </div>
  </FairDialog>
</template>

<script lang="ts" setup>
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useAuthStore } from "~/store/authentication";

const token = ref<string>("");
const authStore = useAuthStore();

const emit = defineEmits(["close"]);

function confirmEmail() {
  authStore.actions.confirmEmailChange(token.value).then(() => close());
}

function close() {
  token.value = "";
  emit("close");
}
</script>

<style scoped></style>
