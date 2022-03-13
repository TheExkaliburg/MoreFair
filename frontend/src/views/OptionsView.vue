<template>
  <div>
    <div v-for="option in options" :key="option">
      <OptionSection :options="option" />
    </div>
  </div>
</template>

<script setup>
//import CheckBox from "@/options/components/CheckBox";
//import RangeSlider from "@/options/components/RangeSlider";
//import NumberInput from "@/options/components/NumberInput";
import OptionSection from "@/options/components/OptionSection";
import { computed } from "vue";
import { useStore } from "vuex";
//import {
//  BoolOption,
//  RangeOption,
//  NumberOption,
//} from "@/options/entities/option";

const store = useStore();
const options = computed(() => store.state.options.options);
</script>
<style lang="scss" scoped>
@import "../styles/styles";
</style>
