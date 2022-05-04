<template>
  <label v-if="visible" :class="active ? '' : 'disabled'">
    <span>{{ option.displayName }}: </span>
    <select :value="option.selectedIndex" class="mySelect" @change="update">
      <option v-for="(option, index) in options" :key="option" :value="index">
        {{ option }}
      </option>
    </select>
  </label>
</template>

<script setup>
import { computed, defineProps } from "vue";
import { useStore } from "vuex";

const store = useStore();

const props = defineProps({
  option: Object,
});

const options = computed(() => props.option.options);
const visible = computed(() => props.option.isVisible());
const active = computed(() => props.option.isActive());

function update({ target }) {
  const newValue = target.value;
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: { selectedIndex: newValue },
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

select.mySelect {
  border: none;
  background: var(--background-color);
  border: 1px dashed var(--main-color);
  box-shadow: 0 0 0px var(--main-color);
  color: var(--main-color);
  padding: 0 10px;

  //animate the border
  &:focus {
    outline: none;
    box-shadow: 0 0 0 1px var(--main-color);
    transition: all 0.2s ease;
    border: 1px dashed var(--background-color);
  }
}

select.mySelect option {
  color: var(--main-color);
  padding: 0 10px;
  border: none;
}
</style>
