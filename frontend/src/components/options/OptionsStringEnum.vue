<template>
  <div
    class="flex flex-row justify-between space-x-2 relative"
    :class="{ 'opacity-50 pointer-events-none': !isActive }"
  >
    <div class="select-none overflow-hidden">{{ formattedName }}:</div>
    <Listbox v-model="selectedValue">
      <ListboxButton
        class="border-1 border-dashed border-button-border px-2 min-w-fit w-32 text-text-dark"
        >{{ option.value }}
      </ListboxButton>
      <transition
        leave-active-class="transition ease-in duration-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <ListboxOptions
          class="absolute min-w-fit w-32 top-2 right-0 bg-background max-h-32 overflow-auto z-1 border-1 border-button-border rounded-md pl-0"
        >
          <ListboxOption
            v-for="entry in options"
            :key="entry"
            :value="entry"
            as="template"
          >
            <li class="w-full flex flex-row cursor-pointer">
              <span
                class="w-full px-2 text-right text-text-light hover:text-button-text-hover hover:bg-button-bg-hover"
                >{{ entry }}</span
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
import { computed, ref, watch } from "vue";
import { useLang } from "~/composables/useLang";

const props = defineProps({
  option: { type: Object, required: true },
  label: { type: String, required: true },
});

const selectedValue = ref<string>(props.option.value);

const options = computed(() => {
  return props.option.transient.options;
});

const lang = useLang("options");

const isActive = computed<boolean>(() => {
  return props.option.isActive();
});

const formattedName = computed(() => {
  return lang(props.label);
});

const emit = defineEmits<{
  (event: "update", value: string): void;
}>();

watch(selectedValue, (value) => {
  emit("update", value);
});
</script>

<style lang="scss" scoped></style>
