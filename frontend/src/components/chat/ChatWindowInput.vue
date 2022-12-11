<template>
  <div class="flex flex-col justify-around">
    <div
      class="flex flex-row justify-center items-center relative w-full bg-background"
    >
      <EditorContent
        :editor="editor"
        class="w-full rounded-l-md border-1 border-button-border p-1 outline-0 overflow-x-hidden text-text caret-text"
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
        editor?.storage.characterCount?.characters()
      }}</span>
      /
      {{ characterLimit }} {{ editor?.getHTML() }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { EditorContent, Node, useEditor } from "@tiptap/vue-3";
import { onBeforeUnmount } from "vue";
import { Paragraph } from "@tiptap/extension-paragraph";
import { Text } from "@tiptap/extension-text";
import { Mention } from "@tiptap/extension-mention";
import { CharacterCount } from "@tiptap/extension-character-count";
import { Placeholder } from "@tiptap/extension-placeholder";
import { useDomUtils } from "~/composables/useDomUtils";
import { useChatStore } from "~/store/chat";
import {
  useEmojiSuggestion,
  useGroupSuggestion,
  useUserSuggestion,
} from "~/composables/useSuggestion";

useDomUtils();

const chatStore = useChatStore();

const input = ref<string>("");
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
        class: "my-0",
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
      suggestion: useUserSuggestion(["Grapes", "Banana", "Apple"]),
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
      suggestion: useGroupSuggestion(["mod", "here", "train"]),
      renderLabel: ({ node }) => {
        return `$${node.attrs.label ?? node.attrs.id}$`;
      },
    }),
  ],
  onUpdate: ({ editor }) => {
    input.value = editor.getText();
  },
});

function sendMessage() {
  const messageJson = editor.value.getJSON();
  const textNodes = messageJson.content[0]?.content;

  let result = "";
  const metadata = [];

  for (const node of textNodes) {
    if (node.type === "text") {
      result += node.text;
    } else if (node.type === "userMention") {
      result += "{@}";
      metadata.push({
        u: node.attrs.id,
        i: result.length - 3,
        id: 0,
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
  }

  chatStore.sendMessage(result, metadata);
  editor.value.commands.clearContent(true);
}

onBeforeUnmount(() => {
  editor.value.destroy();
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
</style>