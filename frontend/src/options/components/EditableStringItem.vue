<template>
  <div class="list-item">
    <input v-model="input" ref="inputElem" />
    <button
      class="btn btn-outline-primary"
      @click="update"
      :disabled="!changed"
    >
      ✓
    </button>
    <button class="btn btn-outline-primary" @click="reset" :disabled="!changed">
      ⟲
    </button>
    <button class="btn btn-outline-primary" @click="remove">X</button>
  </div>
</template>

<script setup>
import { defineProps, computed, ref } from "vue";
import { useStore } from "vuex";

const store = useStore();
const props = defineProps({
  value: String,
  option: Object,
});

const input = ref(props.value);
const inputElem = ref(null);
const changed = computed(() => props.value !== input.value);

function remove() {
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: { method: "remove", value: props.value },
  });
}

function update(target) {
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: {
      method: "edit",
      value: props.value,
      newValue: target.target.previousSibling.value,
    },
  });
  inputElem.value.focus();
}

function reset() {
  input.value = props.value;
  inputElem.value.focus();
}
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

.list-item {
  max-width: 100%;
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
