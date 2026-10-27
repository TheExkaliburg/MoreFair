<template>
  <input
    :value="modelValue"
    class="fair-input"
    spellcheck="false"
    @input="$emit('update:modelValue', getInputTarget($event).value)"
  />
</template>

<script lang="ts" setup>
// TODO: Fix the placeholder text color at some point
defineProps({
  modelValue: { type: String, default: "" },
});

defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();
</script>

<style scoped></style>
