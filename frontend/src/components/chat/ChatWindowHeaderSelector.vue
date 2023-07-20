<template>
  <Listbox as="div" class="relative">
    <ListboxButton aria-label="dropdown-menu" as="template">
      <FairButton class="w-full">
        <div class="flex w-full flex-row justify-between gap-4">
          <ChevronDownIcon class="h-7 w-4 justify-self-start" />
          <span class="align-text-bottom">{{ formattedChatType }}</span>
        </div>
      </FairButton>
    </ListboxButton>
    <transition
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <ListboxOptions
        class="z-1 border-1 absolute right-0 top-5 max-h-32 w-full overflow-auto rounded-md border-button-border bg-background pl-0"
      >
        <ListboxOption v-for="type in chatTypes" :key="type" as="template">
          <li
            class="flex w-full cursor-pointer flex-row justify-between px-2 hover:bg-button-bg-hover hover:text-button-text-hover"
            @click="select($event, type)"
          >
            <input
              :class="{ invisible: !ignorableChatTypes.includes(type) }"
              type="checkbox"
              :checked="!chatStore.state.ignoredChatTypes.has(type)"
              @click.stop
              @input="setIgnoredChatType(type, $event.target.checked)"
            />
            <a class="select-none">{{
              type
                .toLowerCase()
                .replace(/(?:^|\s|-)\S/g, (x) => x.toUpperCase())
            }}</a>
          </li>
        </ListboxOption>
      </ListboxOptions>
    </transition>
  </Listbox>
</template>

<script lang="ts" setup>
import {
  Listbox,
  ListboxButton,
  ListboxOption,
  ListboxOptions,
} from "@headlessui/vue";
import { ChevronDownIcon } from "@heroicons/vue/24/solid";
import { computed } from "vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { ChatType, useChatStore } from "~/store/chat";

const selectableChatTypes = ref<ChatType[]>([ChatType.GLOBAL, ChatType.LADDER]);
const ignorableChatTypes = ref<ChatType[]>([ChatType.LADDER, ChatType.SYSTEM]);

// combine all selectableChatTypes and ignorableChatTypes into one array, without duplicates
const chatTypes = computed<ChatType[]>(() => {
  return [
    ...new Set([...selectableChatTypes.value, ...ignorableChatTypes.value]),
  ];
});

const formattedChatType = computed<string>(() => {
  return chatStore.state.selectedChatType
    .toLowerCase()
    .replace(/(?:^|\s|-)\S/g, (x) => x.toUpperCase());
});

const chatStore = useChatStore();

function setIgnoredChatType(type: ChatType, value: boolean) {
  if (value) {
    chatStore.state.ignoredChatTypes.delete(type);
  } else {
    chatStore.state.ignoredChatTypes.add(type);
  }
}

function select(e: Event, type: ChatType) {
  if (!selectableChatTypes.value.includes(type)) {
    e.preventDefault();
    if (chatStore.state.ignoredChatTypes.has(type)) {
      chatStore.state.ignoredChatTypes.delete(type);
    } else {
      chatStore.state.ignoredChatTypes.add(type);
    }
    return;
  }

  if (
    ignorableChatTypes.value.includes(type) &&
    chatStore.state.ignoredChatTypes.has(type)
  ) {
    chatStore.state.ignoredChatTypes.delete(type);
  }

  chatStore.state.selectedChatType = type;
}
</script>
