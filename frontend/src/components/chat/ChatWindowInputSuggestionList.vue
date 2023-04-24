<template>
  <div>
    <Listbox>
      <ListboxOptions
        ref="list"
        class="absolute bottom-6 bg-background max-h-32 w-min overflow-y-auto z-1 border-1 border-button-border rounded-md pl-0 overflow-x-hidden"
        static
      >
        <ListboxOption
          v-for="(item, index) in formattedItemsArray"
          :key="index"
          as="template"
          class="pl-0"
        >
          <li class="w-full flex flex-row">
            <a
              :ref="index === selectedIndex ? 'selected' : ''"
              :class="{ 'bg-white': index === selectedIndex }"
              class="w-full pr-4 px-2 p text-left text-text-light hover:text-button-text-hover hover:bg-button-bg-hover whitespace-nowrap"
              href="#"
              @click="selectItem(index)"
              @keydown="onKeyDown"
              >{{ item }}</a
            >
          </li>
        </ListboxOption>
      </ListboxOptions>
    </Listbox>
  </div>
</template>
<script lang="ts" setup>
import { Listbox, ListboxOption, ListboxOptions } from "@headlessui/vue";
import { computed, ref, watch } from "vue";

const props = defineProps({
  items: { type: Array, required: true },
  command: {
    type: Function,
    required: true,
  },
  format: {
    type: Function,
    required: true,
  },
});

const formattedItemsArray = computed(() => {
  return props.items.map((item) => props.format(item));
});

const selectedIndex = ref<number>(0);
const list = ref();

watch(props.items, () => {
  selectedIndex.value = 0;
});

function onKeyDown(event: KeyboardEvent) {
  if (event.key === "Escape") {
    event.preventDefault();
    return false;
  }

  if (event.key === "ArrowUp") {
    event.preventDefault();
    selectedIndex.value =
      (selectedIndex.value + props.items.length - 1) % props.items.length;

    list.value?.$el.children[selectedIndex.value]?.scrollIntoView({
      block: "nearest",
    });
    return true;
  }

  if (event.key === "ArrowDown") {
    event.preventDefault();
    selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
    list.value?.$el.children[selectedIndex.value]?.scrollIntoView({
      block: "nearest",
    });
    return true;
  }

  if (
    event.key === "Enter" ||
    event.key === "Tab" ||
    (event.key === " " && event.ctrlKey)
  ) {
    event.preventDefault();
    selectItem(selectedIndex.value);
    return true;
  }

  return false;
}

function selectItem(index: number) {
  const item = props.items[index];
  if (item) {
    props.command({ id: item });
  }
}

defineExpose({ onKeyDown });
</script>
