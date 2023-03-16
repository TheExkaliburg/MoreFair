<template>
  <div class="flex flex-col w-full">
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <div>Power</div>
      <div>Points</div>
    </div>
    <div class="flex flex-row">
      <FairButton class="w-full rounded-r-none"
        >+1 {{ lang("multi") }}
      </FairButton>
      <FairButton class="w-full rounded-l-none border-l-0"
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
      <FairButton class="w-full rounded-r-none"
        >{{ lang("autopromote") }}
      </FairButton>
      <FairButton
        :disabled="true"
        class="w-full rounded-l-none border-l-0 disabled"
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

const lang = useLang("components.ladder.buttons");
</script>

<style scoped></style>
