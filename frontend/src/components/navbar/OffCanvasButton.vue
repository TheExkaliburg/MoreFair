<template>
  <button
    type="button"
    class="flex flex-row justify-start min-w-min py-1"
    @click="onClick()"
  >
    <div class="w-7 h-7 text-button-text"><slot name="icon" /></div>
    <span class="h-7 px-3 w-min text-left">{{ label }}</span>
  </button>
</template>

<script setup lang="ts">
const props = defineProps({
  label: { type: String, required: false, default: "" },
  toggle: { type: Boolean, required: false, default: null },
});

const emit = defineEmits<{
  (e: "onToggle", value: Boolean): void;
}>();

function onClick() {
  if (props.toggle !== null) {
    emit("onToggle", !props.toggle);
  }
}
</script>

<style scoped lang="scss"></style>
