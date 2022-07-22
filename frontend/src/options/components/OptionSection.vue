<template>
  <div v-if="options.isVisible()" class="option-section">
    <h2 class="title">{{ props.options.displayName }}</h2>
    <div v-for="option in props.options.options" :key="option">
      <CheckBox v-if="option instanceof BoolOption" :option="option" />
      <Button v-else-if="option instanceof ButtonOption" :option="option" />
      <EditableStringList
        v-else-if="option instanceof EditableStringListOption"
        :option="option"
      />
      <StringInput
        v-else-if="option instanceof StringInputOption"
        :option="option"
      />
      <RangeSlider v-else-if="option instanceof RangeOption" :option="option" />
      <NumberInput
        v-else-if="option instanceof NumberOption"
        :option="option"
      />
      <DropDown v-else-if="option instanceof DropdownOption" :option="option" />
      <div v-else>
        Unimplemented Option {{ option.name }} {{ option.value }}
      </div>
    </div>
  </div>
</template>

<script setup>
import CheckBox from "@/options/components/CheckBox";
import RangeSlider from "@/options/components/RangeSlider";
import NumberInput from "@/options/components/NumberInput";
import DropDown from "@/options/components/DropDown";
import StringInput from "@/options/components/StringInput";
import Button from "@/options/components/ClickButton";
import EditableStringList from "./EditableStringList.vue";
import { defineProps } from "vue";
import {
  BoolOption,
  NumberOption,
  RangeOption,
  DropdownOption,
  StringInputOption,
  ButtonOption,
  EditableStringListOption,
} from "@/options/entities/option";

const props = defineProps({
  options: Object,
});
</script>
<style lang="scss" scoped>
@import "../../styles/styles";

.option-section {
  border: 1px solid var(--main-color);
  padding: 10px;
  width: 50%;
  position: relative;
  text-align: center;
  margin: 10px;
  margin-left: auto;
  margin-right: auto;
  overflow: hidden;
}

.title {
  border-bottom: 1px solid var(--main-color);
  width: max-content;
  display: inline-block;
}
</style>
