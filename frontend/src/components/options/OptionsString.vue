<template>
  <div class="flex flex-row justify-between space-x-2">
    <div class="w-1/3">{{ formattedName }}:</div>
    <input
      :value="option.value"
      class="bg-transparent border-button-border border-1 border-dashed w-reader max-w-2/3"
      @input="(event) => emit('update', String(event.target.value))"
    />
  </div>
</template>

<script lang="ts" setup>
const props = defineProps({
  option: { type: Object, required: true },
});

const lang = useLang("options");

const formattedName = computed(() => {
  return lang(props.option.key);
});

const emit = defineEmits<{
  (event: "update", value: string): void;
}>();
</script>

<style lang="scss" scoped></style>
