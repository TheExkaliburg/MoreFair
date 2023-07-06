<template>
  <div
    class="flex flex-row justify-between space-x-2"
    :class="{ 'opacity-50 pointer-events-none': !isActive }"
  >
    <div
      :class="{
        'opacity-50': !isActive,
        'cursor-pointer': isActive,
      }"
      class="select-none overflow-hidden"
    >
      {{ formattedName }}:
    </div>
    <input
      v-model="input"
      class="border-1 border-dashed border-button-border px-1 min-w-fit w-32 text-text-dark bg-background text-right"
      :disabled="!isActive"
      type="number"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, watch } from "vue";
import { useLang } from "~/composables/useLang";

const props = defineProps({
  onlyPositive: { type: Boolean, required: false, default: false },
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const input = ref<number | string>(props.option.value);

const isActive = computed<boolean>(() => {
  return props.option.isActive();
});
const lang = useLang("options");

const formattedName = computed(() => {
  return lang(props.label);
});

watch(input, (value: number | string) => {
  if (isActive.value && typeof value === "number") {
    if (props.onlyPositive && value < 0) value = 0;
    emit("update", value);
  }
});

const emit = defineEmits<{
  (event: "update", value: number): void;
}>();
</script>

<style lang="scss" scoped>
input[type="number"] {
  -moz-appearance: textfield;
}
</style>
