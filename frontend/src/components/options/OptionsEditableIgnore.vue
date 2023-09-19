<template>
  <div
    class="flex flex-col justify-between"
    :class="{ 'opacity-50': !isActive }"
  >
    <div class="select-none overflow-hidden">{{ formattedName }}:</div>
    <div
      class="flex flex-col border-1 border-button-border w-fit items-center py-1 space-y-0.5"
    >
      <div class="flex flex-row">
        <input
          :disabled="!isActive"
          :value="input"
          class="bg-transparent border-button-border border-r-0 border-1 border-dashed w-32 px-1"
          @input="input = $event.target.value"
          @keydown.enter="add"
        />
        <button
          :class="{
            'hover:bg-button-bg-hover hover:text-button-text-hover': isActive,
            'pointer-events-none': !isActive,
          }"
          :disabled="!isActive"
          class="border-1 border-button-border rounded-l-none rounded-sm px-1 w-16"
          @click="add"
        >
          Add
        </button>
      </div>
      <div
        v-for="(entry, index) in option.value"
        :key="index"
        class="flex flex-row"
        :class="{
          'select-none pointer-events-none': !isActive,
        }"
      >
        <div
          class="w-32 overflow-hidden whitespace-nowrap border-button-border border-1 border-r-0 pl-1 text-text-dark select-all"
        >
          {{ entry }}
        </div>
        <button
          :disabled="!isActive"
          :class="{
            'hover:bg-button-bg-hover hover:text-button-text-hover': isActive,
            'pointer-events-none': !isActive,
          }"
          class="border-1 border-button-border rounded-l-none rounded-sm px-1 w-16"
          @click="remove(index)"
        >
          X
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import { useLang } from "~/composables/useLang";

const props = defineProps({
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const lang = useLang("options");

const input = ref<string>("");

const isActive = computed<boolean>(() => {
  return props.option.isActive();
});

const formattedName = computed(() => {
  return lang(props.label);
});

const emit = defineEmits<{
  (event: "update", value: string[]): void;
}>();

function add() {
  if (!isActive.value) return;

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
  if (!isActive.value) return;

  const newList = [...props.option.value];
  newList.splice(index, 1);
  emit("update", newList);
}
</script>

<style lang="scss" scoped></style>
