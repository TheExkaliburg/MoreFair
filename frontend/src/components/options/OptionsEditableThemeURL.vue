<template>
  <div
    class="flex flex-col justify-between"
    :class="{ 'opacity-50': !isActive }"
  >
    <div class="select-none overflow-hidden">{{ formattedName }}:</div>
    <div
      class="flex flex-col border border-button-border w-full items-center py-1 space-y-0.5"
    >
      <div class="flex flex-row">
        <input
          v-model="input"
          :disabled="!isActive"
          class="bg-transparent border-button-border border-r-0 border border-dashed w-32 px-1 outline-none"
          @keydown.enter="add"
        />
        <button
          :class="{
            'hover:bg-button-bg-hover hover:text-button-text-hover': isActive,
            'pointer-events-none': !isActive,
          }"
          :disabled="!isActive"
          class="border border-button-border rounded-l-none rounded-sm px-1 w-16"
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
          class="w-64 overflow-hidden whitespace-nowrap border-button-border border border-r-0 pl-1 text-text-dark select-all"
        >
          {{ entry }}
        </div>
        <button
          :disabled="!isActive"
          :class="{
            'hover:bg-button-bg-hover hover:text-button-text-hover': isActive,
            'pointer-events-none': !isActive,
          }"
          class="border border-button-border rounded-l-none rounded-sm px-1 w-16"
          @click="remove(index)"
        >
          X
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useLang } from "~/composables/useLang";
import type { EditableThemeURLOption } from "~/store/entities/option";

const props = defineProps<{ option: EditableThemeURLOption; label: string }>();

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
