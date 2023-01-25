<template>
  <div class="flex flex-row justify-between space-x-2">
    <div class="w-1/3">{{ formattedName }}:</div>
    <input
      :checked="option.value"
      type="checkbox"
      @input="$emit('update', $event.target.checked)"
    />
  </div>
</template>

<script lang="ts" setup>
const props = defineProps({
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const lang = useLang("options");

const formattedName = computed(() => {
  return lang(props.label);
});

defineEmits<{
  (event: "update", value: boolean): void;
}>();
</script>

<style lang="scss" scoped></style>
