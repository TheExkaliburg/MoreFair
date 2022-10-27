<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="10"
      :max="10"
      :prefix="'Chad'"
      class="h-8 w-42 self-end"
      @change="changeChat"
    />
  </div>
</template>

<script lang="ts" setup>
import PaginationButtonGroup from "~/components/interactables/PaginationButtonGroup.vue";

function changeChat() {
  console.log("test");
}
</script>
