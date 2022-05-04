<template>
  <label v-if="visible" :class="active ? '' : 'disabled'">
    <span>{{ option.displayName }}: </span>
    <input :value="value" type="number" @change="update" @input="update" />
  </label>
</template>

<script setup>
import { computed, defineProps } from "vue";
import { useStore } from "vuex";

const store = useStore();

const props = defineProps({
  option: Object,
});

const value = computed(() => props.option.value);
const visible = computed(() => props.option.isVisible());
const active = computed(() => props.option.isActive());

function update({ target }) {
  const newValue = target.value;
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
  background: var(--background-color);
  color: var(--main-color);
  border: 1px dashed var(--main-color);
  box-shadow: 0 0 0px var(--main-color);
  transition: all 0.2s ease;

  //animate the border
  &:focus {
    outline: none;
    box-shadow: 0 0 0 1px var(--main-color);
    transition: all 0.2s ease;
    border: 1px dashed var(--background-color);
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
