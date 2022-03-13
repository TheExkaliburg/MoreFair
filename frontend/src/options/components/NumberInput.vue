<template>
  <label>
    <span>{{ option.displayName }}: </span>
    <input type="number" @change="update" @input="update" :value="value" />
  </label>
</template>

<script setup>
import { defineProps } from "vue";
import { computed } from "vue";
import { useStore } from "vuex";

const store = useStore();

const props = defineProps({
  option: Object,
});

const value = computed(() => props.option.value);

function update({ target }) {
  const newValue = target.value;
  console.log(newValue);
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: { value: newValue },
  });
}
</script>

<style lang="scss" scoped>
@import "../../styles/styles";
label > span {
  //disable selection
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

input {
  width: 100px;
  background: $background-color;
  color: $main-color;
  border: 1px dashed $main-color;
  box-shadow: 0 0 0px $main-color;
  transition: all 0.2s ease;

  //animate the border
  &:focus {
    outline: none;
    box-shadow: 0 0 0 1px $main-color;
    transition: all 0.2s ease;
    border: 1px dashed $background-color;
  }
}

input[type="number"] {
  -webkit-appearance: textfield;
  -moz-appearance: textfield;
  appearance: textfield;
}
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
}
</style>
