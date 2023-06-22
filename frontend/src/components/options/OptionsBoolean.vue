<template>
  <div class="flex flex-row justify-between space-x-2">
    <div
      :class="{
        'opacity-50': !isActive,
        'cursor-pointer': isActive,
      }"
      class="select-none overflow-hidden"
      @click="click"
    >
      {{ formattedName }}:
    </div>
    <input
      :disabled="!isActive"
      :checked="option.value"
      type="checkbox"
      @input="$emit('update', $event.target.checked)"
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

const isActive = computed<boolean>(() => {
  return props.option.isActive();
});
const lang = useLang("options");

const formattedName = computed(() => {
  return lang(props.label);
});

const emit = defineEmits<{
  (event: "update", value: boolean): void;
}>();

function click() {
  if (!isActive.value) return;
  emit("update", !props.option.value);
}
</script>

<style lang="scss" scoped></style>
