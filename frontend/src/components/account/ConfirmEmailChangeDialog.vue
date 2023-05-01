<template>
  <FairDialog
    title="Confirm Your Email"
    description="Please go to your email-inbox and paste the token that was sent to you in the input below:"
    @close="$emit('close')"
  >
    <div class="flex flex-col items-center">
      <div class="h-4" />
      <FairInput :model-value="token" @update:model-value="token = $event" />
      <div class="h-4" />
      <FairButton class="self-end" @click="confirmEmail">Confirm</FairButton>
    </div>
  </FairDialog>
</template>

<script setup lang="ts">
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useAPI } from "~/composables/useAPI";
import { useAccountStore } from "~/store/account";

const token = ref<string>("");

const accountStore = useAccountStore();

const emit = defineEmits(["close"]);

function confirmEmail() {
  useAPI()
    .auth.confirmEmailChange(token.value)
    .then((res) => {
      emit("close");
      accountStore.state.email = res.data;
    });
}
</script>

<style scoped></style>
