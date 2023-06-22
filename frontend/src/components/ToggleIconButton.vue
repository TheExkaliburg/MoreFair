<template>
  <button
    :class="{
      'border-button-border-selected bg-button-bg-selected text-button-text-selected':
        modelValue,
    }"
    class="flex flex-row justify-center min-w-min bg-toggle px-1 py-0 rounded-md"
    type="button"
    @click="emit('update:modelValue', !modelValue)"
  >
    <div v-if="modelValue" class="w-7 h-7">
      <slot name="selectedIcon" />
    </div>
    <div v-else class="w-7 h-7 text-button-text">
      <slot name="icon" />
    </div>
  </button>
</template>

<script lang="ts" setup>
defineProps({
  label: { type: String, required: false, default: "" },
  modelValue: { type: Boolean, required: false, default: null },
});

const emit = defineEmits<{
  (e: "update:modelValue", value: Boolean): void;
}>();
</script>

<style lang="scss" scoped></style>
