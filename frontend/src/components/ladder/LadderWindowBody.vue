<template>
  <div class="flex flex-col w-full">
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <div>Power</div>
      <div>Points</div>
    </div>
    <div class="flex flex-row">
      <FairButton class="w-full rounded-r-none">+1 Multi</FairButton>
      <FairButton class="w-full rounded-l-none border-l-0">+1 Bias</FairButton>
    </div>
    <div
      class="flex flex-row text-xs lg:text-sm w-full justify-between text-text-light"
    >
      <p>Grapes</p>
      <p>Vinegar</p>
    </div>
    <div class="flex flex-row w-full">
      <FairButton class="w-full rounded-r-none">Buy Autopromote</FairButton>
      <FairButton
        :disabled="true"
        class="w-full rounded-l-none border-l-0 disabled"
        >Throw all Vinegar
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
import FairButton from "~/components/interactables/FairButton.vue";
</script>

<style scoped></style>
