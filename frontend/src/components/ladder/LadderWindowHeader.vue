<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="1"
      :max="3"
      :prefix="'Ladder'"
      class="h-8 w-42 self-end bg-background z-2"
      @change="() => {}"
    />
  </div>
</template>

<script lang="ts" setup>
import PaginationButtonGroup from "~/components/interactables/PaginationButtonGroup.vue";
</script>
