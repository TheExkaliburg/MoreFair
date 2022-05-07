<template>
  <label v-if="visible" :class="active ? '' : 'disabled'">
    <button class="btn btn-outline-primary" @click="send">
      {{ option.displayName }}
    </button>
  </label>
</template>

<script setup>
import { computed, defineProps } from "vue";
//import { useStore } from "vuex";

//const store = useStore();

const props = defineProps({
  option: Object,
});

const visible = computed(() => props.option.isVisible());
const active = computed(() => props.option.isActive());

function send() {
  props.option.callback();
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

.btn {
  vertical-align: inherit;
  padding: revert;
}

input {
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
</style>
