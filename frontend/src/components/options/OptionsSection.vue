<template>
  <div>
    <div>{{ label }}:</div>
    <template v-for="entry in currentOptionsArray" :key="entry">
      <OptionsBoolean
        v-if="entry.value instanceof BooleanOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.value = $event"
      ></OptionsBoolean>
      <div v-else>{{ entry.key }}, {{ entry.value.value }}</div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import OptionsBoolean from "~/components/options/OptionsBoolean.vue";
import { BooleanOption } from "~/store/entities/option";

const props = defineProps({
  options: { type: Object, required: true },
  label: { type: String, required: true },
});

const currentOptionsArray = computed(() => {
  return Object.entries(props.options).map(([key, value]) => {
    return {
      key,
      value,
    };
  });
});
</script>

<style lang="scss" scoped></style>
