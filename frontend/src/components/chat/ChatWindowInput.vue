<template>
  <div class="flex flex-col justify-around">
    <div
      class="flex flex-row justify-center items-center relative w-full bg-background z-10"
    >
      <EditorContent
        :editor="editor"
        class="w-full rounded-l-md border-1 h-8 border-button-border p-1 outline-0 overflow-x-hidden text-text caret-text whitespace-nowrap overflow-y-hidden"
        spellcheck="false"
        @keydown.enter.prevent="sendMessage"
        @keydown.tab="wasSuggestionOpen = false"
        @keydown.ctrl.space="wasSuggestionOpen = false"
      ></EditorContent>
      <button
        class="w-1/4 max-w-xs rounded-r-md h-8 border-l-0 border-1 border-button-border py-1 text-button-text hover:text-button-text-hover hover:bg-button-bg-hover"
        @click="sendMessage"
      >
        Send
      </button>
    </div>
    <div class="text-xs lg:text-sm text-left w-full px-2 text-text-light">
      Message length:
      <span class="text-text">{{
        editor?.storage.characterCount?.characters()
      }}</span>
      /
      {{ characterLimit }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { EditorContent, Node, useEditor } from "@tiptap/vue-3";
import { Paragraph } from "@tiptap/extension-paragraph";
import { Text } from "@tiptap/extension-text";
import { Mention } from "@tiptap/extension-mention";
import { CharacterCount } from "@tiptap/extension-character-count";
import { Placeholder } from "@tiptap/extension-placeholder";
import { useChatStore } from "~/store/chat";
import {
  useEmojiSuggestion,
  useGroupSuggestion,
  useUserSuggestion,
} from "~/composables/useSuggestion";

const chatStore = useChatStore();

const characterLimit = 280;

const editor = useEditor({
  extensions: [
    Node.create({
      name: "doc",
      topNode: true,
      content: "block",
    }),
    Text,
    Paragraph.configure({
      HTMLAttributes: {
        class: "my-0 whitespace-pre",
      },
    }),
    CharacterCount.configure({ limit: characterLimit }),
    Placeholder.configure({
      placeholder: "Chad is listening...",
    }),
    Mention.extend({ name: "userMention" }).configure({
      HTMLAttributes: {
        class: "mention",
      },
      suggestion: useUserSuggestion(),
      renderLabel: ({ node }) => {
        return `@${node.attrs.id.username}#${node.attrs.id.accountId}`;
      },
    }),
    Mention.extend({ name: "emojiMention" }).configure({
      HTMLAttributes: {
        class: "mention",
      },
      suggestion: useEmojiSuggestion(),
      renderLabel: ({ node }) => {
        return `${node.attrs.id.emoji}`;
      },
    }),
    Mention.extend({ name: "groupMention" }).configure({
      HTMLAttributes: {
        class: "mention",
      },
      suggestion: useGroupSuggestion(),
      renderLabel: ({ node }) => {
        return `$${node.attrs.label ?? node.attrs.id}$`;
      },
    }),
  ],
  onUpdate: ({ editor }) => {
    chatStore.state.input = editor.getJSON();
  },
});

// TODO: this is kinda hacky, there must be a better way to prevent the enter from autocompletion to also send the message
let isSuggestionOpen = false;
let wasSuggestionOpen = false;
watch(
  () => chatStore.state.input,
  (newValue) => {
    const caretStart: number =
      editor.value?.state.selection.from ?? newValue.length;
    editor.value?.commands.setContent(newValue);
    editor.value?.commands.setTextSelection(caretStart);

    // check if autocomplete is/was open
    const suggestionElements =
      document.getElementsByClassName("tippy-suggestion");
    wasSuggestionOpen = isSuggestionOpen;
    isSuggestionOpen = suggestionElements.length > 0;
  },
  { deep: true }
);

function sendMessage(e: KeyboardEvent | MouseEvent) {
  if (e instanceof KeyboardEvent && (e.key === "Enter" || e.key === "Tab")) {
    if (wasSuggestionOpen) {
      wasSuggestionOpen = false;
      return;
    }
  }

  if (!editor?.value) return;
  const messageJson = editor.value.getJSON();
  if (!messageJson?.content) return;
  const textNodes = messageJson.content[0]?.content;

  if (!textNodes) return;

  let result = "";
  const metadata = [];

  for (const node of textNodes) {
    if (node.type === "text") {
      result += node.text;
      continue;
    }

    if (node?.attrs === undefined) continue;
    if (node.type === "userMention") {
      result += "{@}";
      metadata.push({
        u: node.attrs.id.username,
        i: result.length - 3,
        id: node.attrs.id.accountId,
      });
    } else if (node.type === "emojiMention") {
      result += node.attrs.id.emoji;
    } else if (node.type === "groupMention") {
      result += "{$}";
      metadata.push({
        g: node.attrs.id,
        i: result.length - 3,
      });
    }

    if (result.startsWith(" ")) result = result.trimStart();
  }
  chatStore.actions.sendMessage(result, metadata);
  editor.value.commands.clearContent(true);
}

onBeforeUnmount(() => {
  editor.value?.destroy();
});
</script>
<style lang="scss" scoped>
:deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: var(--text-placeholder-color);
  pointer-events: none;
  height: 0;
}

:deep(.ProseMirror p) {
  max-width: 30vw;

  span {
    white-space: nowrap;
  }
}
</style>
