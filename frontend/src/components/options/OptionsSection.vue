<template>
  <div class="border-button-border border-1 p-1 flex flex-col space-y-0.25">
    <div class="text-2xl text-center text-text-light">{{ formattedName }}</div>
    <template v-for="entry in currentOptionsArray" :key="entry.key">
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
      <OptionsInteger
        v-else-if="entry.value instanceof IntegerOption"
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
      <OptionsEditableMentions
        v-else-if="entry.value instanceof EditableMentionsOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <OptionsEditableIgnore
        v-else-if="entry.value instanceof EditableIgnoreOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <OptionsEditableThemeURL
        v-else-if="entry.value instanceof EditableThemeURLOption"
        :label="entry.key"
        :option="entry.value"
        @update="entry.value.set($event)"
      />
      <div v-else>{{ entry.key }}, {{ entry.value.value }}</div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import OptionsBoolean from "../../components/options/OptionsBoolean.vue";
import OptionsStringEnum from "../../components/options/OptionsStringEnum.vue";
import OptionsRange from "../../components/options/OptionsRange.vue";
import {
  BooleanOption,
  EditableMentionsOption,
  EditableIgnoreOption,
  EditableThemeURLOption,
  EnumOption,
  IntegerOption,
  RangeOption,
} from "~/store/entities/option";
import { useLang } from "~/composables/useLang";
import OptionsEditableMentions from "~/components/options/OptionsEditableMentions.vue";
import OptionsEditableIgnore from "~/components/options/OptionsEditableIgnore.vue";
import OptionsEditableThemeURL from "~/components/options/OptionsEditableThemeURL.vue";
import OptionsInteger from "~/components/options/OptionsInteger.vue";

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
