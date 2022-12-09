<template>
  <div class="w-32 flex flex-row text-text content-center relative">
    <PaginationButtonGroupButton
      :disabled="max === 1"
      class="w-1/6 border-r-0 rounded-l-md"
      @click="emit('change', 1)"
    >
      &laquo;
    </PaginationButtonGroupButton>
    <PaginationButtonGroupButton
      :class="current === 1 ? 'text-button-text-hover bg-button-bg-hover' : ''"
      class="w-1/6"
      @click="emit('change', current === 1 ? current : current - 1)"
    >
      {{ current === 1 ? current : current - 1 }}
    </PaginationButtonGroupButton>
    <PaginationButtonGroupButton
      :class="current !== 1 ? 'text-button-text-hover bg-button-bg-hover' : ''"
      :disabled="max === 1"
      class="w-1/6 border-l-0"
      @click="emit('change', current === 1 ? current + 1 : current)"
    >
      {{ current === 1 ? current + 1 : current }}
    </PaginationButtonGroupButton>
    <PaginationButtonGroupButton
      :disabled="max < Math.max(current, 2) + 1"
      class="w-1/6 border-x-0"
      @click="emit('change', current === 1 ? current + 2 : current + 1)"
    >
      {{ current === 1 ? current + 2 : current + 1 }}
    </PaginationButtonGroupButton>
    <PaginationButtonGroupButton
      :disabled="max === 1"
      class="w-1/6"
      @click="emit('change', max)"
    >
      &raquo;
    </PaginationButtonGroupButton>
    <Listbox>
      <ListboxButton :disabled="max === 1" as="template">
        <PaginationButtonGroupButton
          class="w-1/6 border-l-0 rounded-r-md w-full flex justify-center items-center"
        >
          <ChevronDownIcon class="h-4 w-4 text-button-text" />
        </PaginationButtonGroupButton>
      </ListboxButton>
      <transition
        leave-active-class="transition ease-in duration-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <ListboxOptions
          class="absolute top-5 bg-background max-h-32 w-full overflow-auto z-1 border-1 border-button-border rounded-md pl-0"
        >
          <ListboxOption v-for="number in max" :key="number" as="template">
            <li class="w-full flex flex-row">
              <a
                class="w-full px-2 text-right text-text-light hover:text-button-text-hover hover:bg-button-bg-hover"
                href="#"
                @click="emit('change', number)"
                >{{ prefix }} {{ number }}</a
              >
            </li>
          </ListboxOption>
        </ListboxOptions>
      </transition>
    </Listbox>
  </div>
</template>

<script lang="ts" setup>
import {
  Listbox,
  ListboxButton,
  ListboxOption,
  ListboxOptions,
} from "@headlessui/vue";

import { ChevronDownIcon } from "@heroicons/vue/24/outline";
import PaginationButtonGroupButton from "~/components/interactables/pagination/PaginationButtonGroupButton.vue";

defineProps({
  max: { type: Number, required: true, default: 1 },
  current: { type: Number, required: true, default: 1 },
  size: { type: Number, default: 3 },
  prefix: { type: String, default: "" },
});

const emit = defineEmits<{
  (e: "change", value: Number): void;
}>();
</script>

<style lang="scss" scoped></style>
