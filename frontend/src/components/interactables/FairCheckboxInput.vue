<template>
  <input :checked="modelValue" class="" type="checkbox" @input="change" />
</template>

<script lang="ts" setup>
defineProps({
  modelValue: { type: Boolean, default: false },
});

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
}>();

function change(e) {
  emit("update:modelValue", e.target.checked);
}
</script>

<style scoped></style>
