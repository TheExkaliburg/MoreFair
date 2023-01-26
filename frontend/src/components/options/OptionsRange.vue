<template>
  <div class="flex flex-row justify-between space-x-2">
    <div class="select-none overflow-hidden">
      {{ formattedName }}: {{ option.value }}
    </div>
    <input
      :max="max"
      :min="min"
      :value="option.value"
      class="w-32 min-w-fit"
      type="range"
      @input="$emit('update', $event.target.valueAsNumber)"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps({
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const lang = useLang("options");

const min = computed(() => {
  if (!props.option.transient?.min) {
    return 0;
  }
  return props.option.transient.min;
});

const max = computed(() => {
  if (!props.option.transient?.max) {
    return 100;
  }
  return props.option.transient.max;
});

const formattedName = computed(() => {
  return lang(props.label);
});

defineEmits<{
  (event: "update", value: number): void;
}>();
</script>

<style lang="scss" scoped></style>
