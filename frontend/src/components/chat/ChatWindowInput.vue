<template>
  <div class="flex flex-col justify-around">
    <div class="flex flex-row justify-center items-center relative w-full">
      <EditorContent
        :editor="editor"
        class="w-full rounded-l-md border-1 border-button-border p-1 outline-0 overflow-x-hidden text-text"
        spellcheck="false"
        @keydown.enter.prevent="sendMessage"
      ></EditorContent>
      <button
        class="w-1/4 max-w-xs rounded-r-md border-l-0 border-1 border-button-border py-1 text-button-text hover:text-button-text-hover hover:bg-button-bg-hover"
        @click="sendMessage"
      >
        Send
      </button>
    </div>
    <div class="text-xs lg:text-sm text-left w-full px-2 text-text-light">
      Message length:
      <span class="text-text">{{
        editor?.storage.characterCount.characters()
      }}</span>
      /
      {{ characterLimit }} ->
      <span class="text-text">{{ input }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { EditorContent, Node, useEditor } from "@tiptap/vue-3";
import { Text } from "@tiptap/extension-text";
import { Paragraph } from "@tiptap/extension-paragraph";
import { CharacterCount } from "@tiptap/extension-character-count";
import { Mention } from "@tiptap/extension-mention";
import { onBeforeUnmount } from "vue";
import { useDomUtils } from "~/composables/useDomUtils";
import { useChatStore } from "~/store/chat";
import {
  useEmojiSuggestion,
  useGroupSuggestion,
  useUserSuggestion,
} from "~/composables/useSuggestion";

useDomUtils();

const chatStore = useChatStore();

const input = ref(undefined);
const characterLimit = 280;

const editor = useEditor({
  content: "<p></p>",
  extensions: [
    Node.create({
      name: "doc",
      topNode: true,
      content: "block",
    }),
    Text,
    Paragraph,
    CharacterCount.configure({ limit: characterLimit }),
    Mention.extend({ name: "userMention" }).configure({
      HTMLAttributes: {
        class: "user",
      },
      suggestion: useUserSuggestion(["Grapes", "Banana", "Apple"]),
    }),
    Mention.extend({ name: "emojiMention" }).configure({
      HTMLAttributes: {
        class: "emoji",
      },
      suggestion: useEmojiSuggestion(),
      renderLabel: ({ node }) => {
        return `${node.attrs.label ?? node.attrs.id.emoji}`;
      },
    }),
    Mention.extend({ name: "groupMention" }).configure({
      HTMLAttributes: {
        class: "group",
      },
      suggestion: useGroupSuggestion(["mod", "here", "train"]),
      renderLabel: ({ node }) => {
        return `$${node.attrs.label ?? node.attrs.id}$`;
      },
    }),
  ],
  onUpdate: ({ editor }) => {
    input.value = editor.getText();
    console.log(editor.getJSON());
  },
});

function sendMessage() {
  chatStore.sendMessage(input.value, []);
  editor.value.commands.clearContent(true);
}

onBeforeUnmount(() => {
  editor.value.destroy();
});
</script>
