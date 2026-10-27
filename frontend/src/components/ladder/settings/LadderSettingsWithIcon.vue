<template>
  <FairButton
    class="inline-flex items-center justify-center rounded-full h-8 w-8 align-middle px-0 m-0"
  >
    <slot />
  </FairButton>
</template>

<script setup lang="ts">
import FairButton from "~/components/interactables/FairButton.vue";
</script>

<style scoped lang="scss"></style>
