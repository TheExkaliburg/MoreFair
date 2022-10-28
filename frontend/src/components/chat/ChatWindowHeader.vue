<template>
  <div class="flex justify-end items-center text-button-text">
    <PaginationButtonGroup
      :current="current"
      :max="10"
      :prefix="'Chad'"
      class="h-8 w-42 self-end"
      @change="changeChat"
    />
  </div>
</template>

<script lang="ts" setup>
import PaginationButtonGroup from "~/components/interactables/PaginationButtonGroup.vue";

const current = ref<number>(1);

function changeChat(e: number) {
  current.value = e;
}
</script>
