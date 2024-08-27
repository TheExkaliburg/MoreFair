<template>
  <LadderSettingsWithIcon data-tutorial="help" @click="open">
    <QuestionMarkCircleIcon />
  </LadderSettingsWithIcon>
  <FairDialog :open="isOpen" :title="lang('tutorials')" @close="isOpen = false">
    <div class="flex flex-col text-xl text-link-text select-none">
      <a
        class="hover:text-link-text-hover hover:cursor-pointer"
        @click="start(useStartupTour)"
        >1. {{ lang("first") }}</a
      >
      <a
        class="hover:text-link-text-hover hover:cursor-pointer"
        @click="start(useBiasedTour)"
        >2. {{ lang("upgrades") }}</a
      >
      <a
        class="hover:text-link-text-hover hover:cursor-pointer"
        @click="start(useMultiedTour)"
        >3. {{ lang("climbing") }}</a
      >
      <a
        class="hover:text-link-text-hover hover:cursor-pointer"
        @click="start(useAutoPromoteTour)"
        >4. {{ lang("grapes") }}</a
      >
      <a
        class="hover:text-link-text-hover hover:cursor-pointer"
        @click="start(useVinegarTour)"
        >5. {{ lang("vinegar") }}</a
      >
      <a
        class="hover:text-link-text-hover hover:cursor-pointer"
        @click="start(usePromoteTour)"
        >6. {{ lang("goodluck") }}</a
      >
    </div>
  </FairDialog>
</template>

<script setup lang="ts">
import { QuestionMarkCircleIcon } from "@heroicons/vue/24/outline";
import FairDialog from "~/components/interactables/FairDialog.vue";
import { Tour } from "~/composables/useTour";

const lang = useLang("tour.names");
const isOpen = ref<boolean>(false);

function open(): void {
  isOpen.value = true;
}

function start(tourFn: () => Tour): void {
  isOpen.value = false;
  tourFn().start();
}
</script>
