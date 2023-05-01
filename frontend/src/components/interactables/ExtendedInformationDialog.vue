<template>
  <button class="w-full" @click="open = true">
    <slot name="button" />
    <FairDialog
      :open="open"
      :title="title"
      :description="description"
      @close="open = false"
    >
      <template #title><slot name="title" /></template>
      <template #description><slot name="description" /></template>
      <template #default>
        <slot />
      </template>
    </FairDialog>
  </button>
</template>

<script lang="ts" setup>
import FairDialog from "./FairDialog.vue";

const open = ref<boolean>(false);

defineProps({
  title: {
    type: String,
    required: false,
    default: "",
  },
  description: {
    type: String,
    required: false,
    default: "",
  },
});
</script>

<style scoped lang="scss"></style>
