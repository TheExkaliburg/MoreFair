<template>
  <FairDialog
    :click-outside-to-close="false"
    :description="lang('description') + ':'"
    :title="lang('title')"
    @close="close"
  >
    <div class="flex flex-col items-center">
      <div class="h-4" />
      <FairInput v-model="token" />
      <div class="h-4" />
      <FairButton
        :disabled="token.length === 0"
        class="self-end"
        @click="confirmEmail"
        >{{ lang("submit") }}
      </FairButton>
    </div>
  </FairDialog>
</template>

<script lang="ts" setup>
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairInput from "~/components/interactables/FairInput.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useAuthStore } from "~/store/authentication";
import { useLang } from "~/composables/useLang";

const token = ref<string>("");
const authStore = useAuthStore();

const lang = useLang("account.confirmEmail");
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
