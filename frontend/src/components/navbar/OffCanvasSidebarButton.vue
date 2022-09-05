<template>
  <button
    type="button"
    class="flex flex-row justify-start min-w-min py-1 bg-toggle"
    @click="emit('update:modelValue', !modelValue)"
  >
    <div class="w-7 h-7 text-button-text">
      <slot name="icon" />
    </div>
    <VerticalToggleSwitch
      v-if="isToggleSwitch"
      :model-value="modelValue"
      @update:modelValue="(value) => emit('update:modelValue', value)"
    />
    <div v-else class="w-7 h-5"></div>
    <span class="h-7 px-1 w-min text-left">{{ label }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from "vue";
import VerticalToggleSwitch from "~/components/VerticalToggleSwitch.vue";

const props = defineProps({
  label: { type: String, required: false, default: "" },
  modelValue: { type: Boolean, required: false, default: null },
});

const emit = defineEmits<{
  (e: "update:modelValue", value: Boolean): void;
}>();

const isToggleSwitch = computed<Boolean>(() => props.modelValue !== null);
</script>

<style scoped lang="scss"></style>
