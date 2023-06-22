<template>
  <div class="flex flex-row justify-between space-x-2">
    <div
      class="select-none overflow-hidden"
      :class="{ 'opacity-50': !isActive }"
    >
      {{ formattedName }}: {{ option.value }}
    </div>
    <input
      :disabled="!isActive"
      :max="max"
      :min="min"
      :value="option.value"
      class="w-32 min-w-fit"
      type="range"
      @input="change"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { useLang } from "../../composables/useLang";

const props = defineProps({
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const lang = useLang("options");

const isActive = computed<boolean>(() => {
  return props.option.isActive();
});

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

const emit = defineEmits<{
  (event: "update", value: number): void;
}>();

function change(e: Event) {
  if (!isActive.value) return;
  emit("update", e.target.valueAsNumber);
}
</script>

<style lang="scss" scoped></style>
