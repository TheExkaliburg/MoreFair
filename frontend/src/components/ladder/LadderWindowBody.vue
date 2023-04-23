<template>
  <div class="flex flex-col w-full">
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <div>Power</div>
      <div>Points</div>
    </div>
    <div class="flex flex-row">
      <FairButton :disabled="isButtonLocked" class="w-full rounded-r-none"
        >+1 {{ lang("multi") }}
      </FairButton>
      <FairButton
        :disabled="isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        >+1 {{ lang("bias") }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>Grapes</p>
      <p>Vinegar</p>
    </div>
    <div class="flex flex-row w-full">
      <FairButton :disabled="isButtonLocked" class="w-full rounded-r-none"
        >{{ lang("autopromote") }}
      </FairButton>
      <FairButton
        :disabled="isButtonLocked"
        class="w-full rounded-l-none border-l-0"
        >{{ lang("vinegar") }}
      </FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>Promote at</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import FairButton from "../../components/interactables/FairButton.vue";
import { useLang } from "~/composables/useLang";
import { useOptionsStore } from "~/store/options";

const lang = useLang("components.ladder.buttons");
const optionsStore = useOptionsStore();
const isButtonLocked = computed<boolean>(() => {
  return optionsStore.state.ladder.lockButtons.value;
});
</script>

<style scoped></style>
