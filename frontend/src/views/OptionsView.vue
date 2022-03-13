<template>
  <div>
    <div v-for="option in options" :key="option">
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
import { computed } from "vue";
import { useStore } from "vuex";
import {
  BoolOption,
  RangeOption,
  NumberOption,
} from "@/options/entities/option";

const store = useStore();
const options = computed(() => store.state.options.options);
</script>
<style lang="scss" scoped>
@import "../styles/styles";
</style>
