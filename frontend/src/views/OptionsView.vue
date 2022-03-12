<template>
  <div>
    <div v-for="option in options" :key="option">
      <CheckBox v-if="option instanceof BoolOption" :option="option" />
      <div v-if="option instanceof RangeOption">
        {{ option.name }} {{ option.value }}
      </div>
    </div>
  </div>
</template>

<script setup>
import CheckBox from "@/options/components/CheckBox";
import { computed } from "vue";
import { useStore } from "vuex";
import { BoolOption, RangeOption } from "@/options/entities/option";

const store = useStore();
const options = computed(() => store.state.options.options);
</script>
<style lang="scss" scoped>
@import "../styles/styles";
</style>
