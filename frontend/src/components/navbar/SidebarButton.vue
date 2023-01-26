<template>
  <button
    class="flex flex-row justify-start min-w-min py-1 bg-toggle"
    type="button"
    @click="emit('update:modelValue', !modelValue)"
  >
    <ToggleIconButton
      v-if="isToggleSwitch"
      :model-value="modelValue"
      @update:modelValue="emit('update:modelValue', $event)"
    >
      <template #icon>
        <slot name="icon"></slot>
      </template>
      <template #selectedIcon>
        <slot name="selectedIcon"></slot>
      </template>
    </ToggleIconButton>
    <div v-else class="h-7 w-7 text-button-text">
      <slot name="icon"></slot>
    </div>
    <span v-if="label" class="h-7 pl-8 px-2 w-fit text-left">{{ label }}</span>
  </button>
</template>

<script lang="ts" setup>
import ToggleIconButton from "~/components/ToggleIconButton.vue";

const props = defineProps({
  label: { type: String, required: false, default: "" },
  modelValue: { type: Boolean, required: false, default: null },
});

const isToggleSwitch = computed<Boolean>(() => props.modelValue !== null);

const emit = defineEmits<{
  (e: "update:modelValue", value: Boolean): void;
}>();
</script>

<style lang="scss" scoped></style>
