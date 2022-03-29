<template>
  <label>
    <span>{{ option.displayName }}: {{ value }}</span
    ><br />
    <input
      :max="max"
      :min="min"
      :value="value"
      type="range"
      @change="update"
      @input="update"
    />
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
const min = computed(() => props.option.min);
const max = computed(() => props.option.max);

function update({ target }) {
  const newValue = target.value;
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: { value: parseInt(newValue) },
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
</style>
