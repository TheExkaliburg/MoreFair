<template>
  <button type="button" class="flex flex-row justify-start min-w-min">
    <div class="w-7 h-7 text-button-text"><slot name="icon" /></div>
    <span class="h-7 px-3 w-min text-left">{{ label }}</span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
}>();
</script>

<style scoped lang="scss"></style>
