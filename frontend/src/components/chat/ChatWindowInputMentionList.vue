@@ -0,0 +1,69 @@
<template>
  <div>
    <Listbox>
      <ListboxOptions
        class="absolute bottom-6 bg-background max-h-32 w-min overflow-auto z-1 border-1 border-button-border rounded-md pl-0"
        static
      >
        <ListboxOption
          v-for="(item, index) in items"
          :key="index"
          as="template"
          class="pl-0"
        >
          <li class="w-full flex flex-row">
            <a
              :class="{ 'bg-white': index === selectedIndex }"
              class="w-full px-2 text-right text-text-light hover:text-button-text-hover hover:bg-button-bg-hover"
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
<!--template>
  <div>
    <template v-if="items.length > 0">
      <button
        v-for="(item, index) in items"
        :key="index"
        :class="{ 'bg-white': index === selectedIndex }"
        @click="selectItem(index)"
        @keydown="onKeyDown"
      >
        {{ item }}
      </button>
    </template>
    <div v-else>No result</div>
  </div>
</template-->
<!--script lang="ts" setup>
import { Listbox, ListboxOption, ListboxOptions } from "@headlessui/vue";
import { watch } from "vue";

const props = defineProps({
  items: { type: Array, required: true },
  command: {
    type: Function,
    required: true,
  },
});

const selectedIndex = ref<number>(0);

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
    return true;
  }

  if (event.key === "ArrowDown") {
    event.preventDefault();
    selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
    return true;
  }

  if (event.key === "Enter" || event.key === "Tab") {
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
</script-->

<script lang="ts">
import { Listbox, ListboxOption, ListboxOptions } from "@headlessui/vue";

export default {
  components: {
    Listbox,
    ListboxOption,
    ListboxOptions,
  },
  props: {
    items: {
      type: Array,
      required: true,
    },
    command: {
      type: Function,
      required: true,
    },
  },
  data() {
    return {
      selectedIndex: 0,
    };
  },
  watch: {
    items() {
      this.selectedIndex = 0;
    },
  },
  methods: {
    onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        event.preventDefault();
        return false;
      }

      if (event.key === "ArrowUp") {
        event.preventDefault();
        this.selectedIndex =
          (this.selectedIndex + this.items.length - 1) % this.items.length;
        return true;
      }

      if (event.key === "ArrowDown") {
        event.preventDefault();
        this.selectedIndex = (this.selectedIndex + 1) % this.items.length;
        return true;
      }

      if (event.key === "Enter" || event.key === "Tab") {
        event.preventDefault();
        this.selectItem(this.selectedIndex);
        return true;
      }

      return false;
    },
    selectItem(index: number) {
      const item = this.items[index];
      if (item) {
        this.command({ id: item });
      }
    },
  },
};
</script>
