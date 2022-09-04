<template>
  <div class="flex flex-row h-7 w-7 items-center justify-center">
    <div
      :class="modelValue ? 'bg-toggle-bg-on' : 'bg-toggle-bg-off'"
      class="relative justify-center rounded-full inline-flex h-6 w-3 border border-toggle-border"
    >
      <span
        :class="
          modelValue
            ? 'translate-y-0 bg-toggle-circle-bg-on'
            : 'translate-y-3 bg-toggle-circle-bg-off'
        "
        class="inline-block rounded-full h-2.5 w-2.5 transform border border-toggle-circle-border"
      ></span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  modelValue: { type: Boolean, required: true, default: false },
});

defineEmits<{
  (e: "update:modelValue", value: Boolean): void;
}>();
</script>
