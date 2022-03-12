<template>
  <label>
    <input type="checkbox" @change="update" :checked="value" />
    <span>{{ option.name }}</span>
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
  const newChecked = target.checked;
  console.log(newChecked);
  store.commit({
    type: "options/updateOption",
    option: props.option,
    value: newChecked,
  });
}

//store.commit({type: "options/registerNewOption", option: new BoolOption({ name: props.optionID, value: props.checked }) });
</script>

<style lang="scss" scoped>
@import "../../styles/styles";
label > span {
  margin-left: 10px;
  //disable selection
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
</style>
