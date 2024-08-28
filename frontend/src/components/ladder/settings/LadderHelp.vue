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

      <div class="flex flex-row justify-between pt-10">
        <FairButton @click="finishTutorials">Skip Tutorials</FairButton>
        <FairButton @click="resetTutorials">Reset Tutorials</FairButton>
      </div>
    </div>
  </FairDialog>
</template>

<script setup lang="ts">
import { QuestionMarkCircleIcon } from "@heroicons/vue/24/outline";
import FairDialog from "~/components/interactables/FairDialog.vue";
import { Tour } from "~/composables/useTour";
import FairButton from "~/components/interactables/FairButton.vue";

const lang = useLang("tour.names");
const isOpen = ref<boolean>(false);

function open(): void {
  isOpen.value = true;
}

function start(tourFn: () => Tour): void {
  isOpen.value = false;
  tourFn().start();
}

function finishTutorials() {
  const flags = useStartupTour().flags;
  flags.value.shownStartup = true;
  flags.value.shownBiased = true;
  flags.value.shownMultied = true;
  flags.value.shownAutoPromote = true;
  flags.value.shownVinegar = true;
  flags.value.shownPromoted = true;
}

function resetTutorials() {
  isOpen.value = false;
  const flags = useStartupTour().flags;
  flags.value.shownStartup = false;
  flags.value.shownBiased = false;
  flags.value.shownMultied = false;
  flags.value.shownAutoPromote = false;
  flags.value.shownVinegar = false;
  flags.value.shownPromoted = false;
}
</script>
