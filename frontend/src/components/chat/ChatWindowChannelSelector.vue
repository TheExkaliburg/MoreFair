<template>
  <Listbox as="div" class="relative">
    <ListboxButton aria-label="dropdown-menu" as="template">
      <button class="w-full border-0">
        <div class="flex w-full flex-row justify-between gap-4">
          <!--ChevronDownIcon class="h-7 w-4 justify-self-start" /-->
          <span
            class="pt-1 align-text-bottom text-text"
            :class="{
              'text-teal-600':
                chatStore.state.selectedChatType === ChatType.LADDER,
              'text-violet-600':
                chatStore.state.selectedChatType === ChatType.MOD,
              'text-text': chatStore.state.selectedChatType === ChatType.GLOBAL,
            }"
            >/{{ formattedChatType }}</span
          >
        </div>
      </button>
    </ListboxButton>
    <transition
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <ListboxOptions
        class="z-1 border-1 absolute left-0 bottom-5 max-h-32 w-full overflow-auto rounded-md border-button-border bg-background pl-0 min-w-min"
      >
        <ListboxOption v-for="type in chatTypes" :key="type" as="template">
          <li
            class="flex w-full cursor-pointer flex-row gap-2 justify-between px-2 hover:bg-button-bg-hover hover:text-button-text-hover"
            @click="select($event, type)"
          >
            <a
              class="select-none whitespace-nowrap text-text"
              :class="{
                'text-teal-600': type === ChatType.LADDER,
                'text-violet-600': type === ChatType.MOD,
                'text-text': type === ChatType.GLOBAL,
              }"
              >/{{ lang(type + ".identifier").toLowerCase() }}, /{{
                type.toLowerCase()
              }}</a
            >
            <input
              :class="{ invisible: ignorableChatTypes.includes(type) }"
              type="checkbox"
              :checked="!chatStore.state.ignoredChatTypes.has(type)"
              @click.stop
              @input="setIgnoredChatType(type, $event.target.checked)"
            />
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
import { computed } from "vue";
import { ChatType, useChatStore } from "~/store/chat";
import { useAccountStore } from "~/store/account";
import { useLang } from "~/composables/useLang";

const chatStore = useChatStore();

const selectableChatTypes = computed<ChatType[]>(() => {
  const result = [ChatType.GLOBAL, ChatType.LADDER];
  if (useAccountStore().getters.isMod) {
    result.push(ChatType.MOD);
  }
  return result;
});
const ignorableChatTypes = ref<ChatType[]>([ChatType.LADDER]);

if (useAccountStore().getters.isMod) {
  selectableChatTypes.value.push(ChatType.MOD);
}
const lang = useLang("chat");

// combine all selectableChatTypes and ignorableChatTypes into one array, without duplicates
const chatTypes = computed<ChatType[]>(() => {
  return [
    ...new Set([...selectableChatTypes.value, ...ignorableChatTypes.value]),
  ];
});

const formattedChatType = computed<string>(() => {
  const s = lang(chatStore.state.selectedChatType + ".identifier");
  return s.toLowerCase();
});

function setIgnoredChatType(type: ChatType, value: boolean) {
  if (value) {
    chatStore.state.ignoredChatTypes.delete(type);
  } else {
    chatStore.state.ignoredChatTypes.add(type);
    if (chatStore.state.selectedChatType === type) {
      chatStore.state.selectedChatType = ChatType.GLOBAL;
    }
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
