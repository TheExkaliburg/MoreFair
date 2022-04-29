<template>
  <label v-if="visible" :class="active ? '' : 'disabled'">
    <input :checked="value" type="checkbox" @change="update" />
    <span>{{ option.displayName }}</span>
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
  const newChecked = target.checked;
  store.commit({
    type: "options/updateOption",
    option: props.option,
    payload: { value: newChecked },
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
