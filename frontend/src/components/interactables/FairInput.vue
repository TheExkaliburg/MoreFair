<template>
  <input
    :value="modelValue"
    class="w-full rounded-md border placeholder:text-text-placeholder h-8 border-button-border p-1 outline-0 text-text caret-text whitespace-nowrap overflow-x-auto overflow-y-hidden bg-background disabled:bg-opacity-50"
    spellcheck="false"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<script lang="ts" setup>
// TODO: Fix the placeholder text color at some point
defineProps({
  modelValue: { type: String, default: "" },
});

defineEmits<{
  (event: "update:modelValue", value: String): void;
}>();
</script>

<style scoped></style>
