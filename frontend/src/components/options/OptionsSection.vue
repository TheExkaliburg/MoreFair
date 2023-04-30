<template>
  <div class="border-button-border border-1 p-1 flex flex-col space-y-0.25">
    <div class="text-2xl text-center text-text-light">{{ formattedName }}</div>
    <template v-for="entry in currentOptionsArray" :key="entry">
      <OptionsBoolean
        v-if="entry.value instanceof BooleanOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <OptionsStringEnum
        v-else-if="entry.value instanceof EnumOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <OptionsRange
        v-else-if="entry.value instanceof RangeOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <OptionsEditableStringList
        v-else-if="entry.value instanceof EditableStringListOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <div v-else>{{ entry.key }}, {{ entry.value.value }}</div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import OptionsBoolean from "../../components/options/OptionsBoolean.vue";
import OptionsStringEnum from "../../components/options/OptionsStringEnum.vue";
import OptionsRange from "../../components/options/OptionsRange.vue";
import OptionsEditableStringList from "../../components/options/OptionsEditableStringList.vue";
import {
  BooleanOption,
  EditableStringListOption,
  EnumOption,
  RangeOption,
} from "~/store/entities/option";
import { useLang } from "~/composables/useLang";

const props = defineProps({
  options: { type: Object, required: true },
  label: { type: String, required: true },
});

const lang = useLang("options.section");

const formattedName = computed(() => {
  return lang(props.label);
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
