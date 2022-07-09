<template>
  <label v-if="visible" :class="active ? '' : 'disabled'">
    <span>{{ option.displayName }}: </span>
    <div class="list-container">
      <div>
        <input />
        <button class="btn btn-outline-primary" @click="add">Add</button>
      </div>
      <EditableStringItem
        v-for="item in options"
        :key="item"
        :option="props.option"
        :value="item"
      />
    </div>
  </label>
</template>

<script setup>
import { computed, defineProps } from "vue";
import EditableStringItem from "./EditableStringItem.vue";
import { useStore } from "vuex";

const store = useStore();

const props = defineProps({
  option: Object,
});

const options = computed(() => props.option.get());

const visible = computed(() => props.option.isVisible());
const active = computed(() => props.option.isActive());

function add(target) {
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: { method: "add", value: target.target.previousSibling.value },
  });
  target.target.previousSibling.value = "";
}

//modules.commit({type: "options/registerNewOption", option: new BoolOption({ name: props.optionID, value: props.checked }) });
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

.list-container {
  display: flex;
  flex-direction: column;
  align-items: center;

  padding: 5px;
  border: 1px solid var(--main-color);
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

.brLine {
  width: 100%;
  border: none;
  margin: 0;
}
</style>
