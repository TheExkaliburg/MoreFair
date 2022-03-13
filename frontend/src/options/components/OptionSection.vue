<template>
  <div class="option-section">
    <h2 class="title">{{ props.options.displayName }}</h2>
    <div class="toggle">X</div>
    <div v-for="option in props.options.options" :key="option">
      <CheckBox v-if="option instanceof BoolOption" :option="option" />
      <RangeSlider v-else-if="option instanceof RangeOption" :option="option" />
      <NumberInput
        v-else-if="option instanceof NumberOption"
        :option="option"
      />
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
import { defineProps } from "vue";
import {
  BoolOption,
  RangeOption,
  NumberOption,
} from "@/options/entities/option";

const props = defineProps({
  options: Object,
});
</script>
<style lang="scss" scoped>
@import "../../styles/styles";
.option-section {
  border: 1px solid $main-color;
  padding: 10px;
  width: max-content;
  position: relative;
}
.title {
  border-bottom: 1px solid $main-color;
  width: max-content;
  display: inline-block;
}
.toggle {
  border: 1px solid $main-color;
  border-radius: 50%;
  cursor: pointer;
  height: 20px;
  width: 20px;
  display: inline-block;
  position: absolute;
  top: 17px;
  right: 17px;
  text-align: center;
  line-height: 17px;

  //disable selection
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
</style>
