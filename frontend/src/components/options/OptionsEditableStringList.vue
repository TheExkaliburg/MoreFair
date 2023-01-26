<template>
  <div class="flex flex-col justify-between">
    <div
      class="select-none overflow-hidden"
      @click="$emit('update', !option.value)"
    >
      {{ formattedName }}:
    </div>
    <div
      class="flex flex-col border-1 border-button-border w-fit items-center py-1 space-y-0.5"
    >
      <div class="flex flex-row">
        <input
          :value="input"
          class="bg-transparent border-button-border border-r-0 border-1 border-dashed w-32 px-1"
          @input="input = $event.target.value"
          @keydown.enter="add"
        />
        <button
          class="border-1 border-button-border rounded-l-none rounded-sm px-1 w-16 hover:bg-button-bg-hover hover:text-button-text-hover"
          @click="add"
        >
          Add
        </button>
      </div>
      <div
        v-for="(entry, index) in option.value"
        :key="index"
        class="flex flex-row"
      >
        <div
          class="w-32 overflow-hidden whitespace-nowrap border-button-border border-1 border-r-0 pl-1 text-text-dark select-all"
        >
          ${{ entry }}$
        </div>
        <button
          class="border-1 border-button-border rounded-l-none rounded-sm px-1 w-16 hover:bg-button-bg-hover hover:text-button-text-hover"
          @click="remove(index)"
        >
          X
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
const props = defineProps({
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const lang = useLang("options");

const input = ref<string>("");

const formattedName = computed(() => {
  return lang(props.label);
});

const emit = defineEmits<{
  (event: "update", value: string[]): void;
}>();

function add() {
  if (!Array.isArray(props.option.value)) {
    emit("update", []);
    return;
  }

  if (input.value === "" || props.option.value.includes(input.value)) {
    return;
  }

  const newList = [...props.option.value, input.value];
  emit("update", newList);
  input.value = "";
}

function remove(index: number) {
  const newList = [...props.option.value];
  newList.splice(index, 1);
  emit("update", newList);
}
</script>

<style lang="scss" scoped></style>
