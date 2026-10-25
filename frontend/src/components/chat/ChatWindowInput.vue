<template>
  <div class="flex flex-col justify-around">
    <div
      class="flex flex-row relative justify-center items-center w-full bg-background z-10"
    >
      <ChatWindowChannelSelector
        class="z-20 w-8 h-8 pl-2 px-0 overflow-visible whitespace-nowrap border-button-border border-1 border-r-0 rounded-l-md"
      />
      <EditorContent
        :editor="editor"
        class="justify-center items-center w-full border-1 border-l-0 h-8 border-button-border p-1 w-full max-w-full outline-0 overflow-x-hidden text-text caret-text whitespace-nowrap overflow-y-hidden"
        spellcheck="false"
        @keydown.enter="sendMessage"
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
import type { Attributes, JSONContent } from "@tiptap/core";
import { Paragraph } from "@tiptap/extension-paragraph";
import { Text } from "@tiptap/extension-text";
import { Mention, type MentionOptions } from "@tiptap/extension-mention";
import { CharacterCount, Placeholder } from "@tiptap/extensions";
import ChatWindowChannelSelector from "./ChatWindowChannelSelector.vue";
import { ChatType, useChatStore, type Suggestion } from "~/store/chat";
import {
  useEmojiSuggestion,
  useGroupSuggestion,
  useUserSuggestion,
  type EmojiDataEntry,
} from "~/composables/useSuggestion";
import { useLang } from "~/composables/useLang";
import { useAccountStore } from "~/store/account";

const chatStore = useChatStore();

const characterLimit = 280;

const ChatMention = Mention.extend({
  addAttributes() {
    const attributes: Attributes = this.parent?.() ?? {};
    return {
      ...attributes,
      mentionSuggestionChar: {
        ...attributes?.mentionSuggestionChar,
        default: this.options.suggestion.char ?? "@",
      },
    };
  },
}).configure({
  renderHTML: (props) => [
    "span",
    props.options.HTMLAttributes,
    props.options.renderText(props),
  ],
});

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
    CharacterCount.configure({ limit: characterLimit, autoTrim: false }),
    Placeholder.configure({
      placeholder: "Chad is listening...",
    }),
    ChatMention.extend<MentionOptions<Suggestion, { id: Suggestion }>>({
      name: "userMention",
    }).configure({
      HTMLAttributes: {
        class: "mention",
      },
      suggestion: useUserSuggestion(),
      renderText: ({ node }) => {
        return `@${node.attrs.id.displayName}#${node.attrs.id.accountId}`;
      },
    }),
    ChatMention.extend<MentionOptions<EmojiDataEntry, { id: EmojiDataEntry }>>({
      name: "emojiMention",
    }).configure({
      HTMLAttributes: {
        class: "mention",
      },
      suggestion: useEmojiSuggestion(),
      renderText: ({ node }) => {
        return `${node.attrs.id.emoji}`;
      },
    }),
    ChatMention.extend<MentionOptions<string, { id: string }>>({
      name: "groupMention",
    }).configure({
      HTMLAttributes: {
        class: "mention",
      },
      suggestion: useGroupSuggestion(),
      renderText: ({ node }) => {
        return `$${node.attrs.label ?? node.attrs.id}$`;
      },
    }),
  ],
  onUpdate: ({ editor }) => {
    chatStore.state.input = editor.getJSON();
  },
});

const lang = useLang("chat");

watch(
  () => chatStore.state.input,
  (newValue) => {
    const currentEditor = editor.value;
    if (!currentEditor) return;
    if (
      currentEditor.getText().trimStart().startsWith("/") &&
      currentEditor.getText().endsWith(" ")
    ) {
      const channel = editor.value?.getText().trim().slice(1).toUpperCase();
      switch (channel) {
        case ChatType.MOD:
        case lang(ChatType.MOD + ".identifier"):
          if (useAccountStore().getters.isMod) {
            useChatStore().state.selectedChatType = ChatType.MOD;
            currentEditor.commands.clearContent(true);
            return;
          }
          break;
        case ChatType.LADDER:
        case lang(ChatType.LADDER + ".identifier"):
        case "LOCAL":
          useChatStore().state.selectedChatType = ChatType.LADDER;
          currentEditor.commands.clearContent(true);
          return;
        case ChatType.GLOBAL:
        case lang(ChatType.GLOBAL + ".identifier"):
        case "GENERAL":
          useChatStore().state.selectedChatType = ChatType.GLOBAL;
          currentEditor.commands.clearContent(true);
          return;
        default:
          break;
      }
    }

    if (
      !currentEditor.schema.nodeFromJSON(newValue).eq(currentEditor.state.doc)
    ) {
      const caretStart = currentEditor.state.selection.from;
      currentEditor.commands.setContent(newValue, { emitUpdate: false });
      currentEditor.commands.setTextSelection(caretStart);
    }
  },
  { deep: true },
);

function sendMessage(e: KeyboardEvent | MouseEvent) {
  if (e instanceof KeyboardEvent) {
    if (e.isComposing) return;
    e.preventDefault();
  }

  if (!editor?.value) return;
  const messageJson: JSONContent = editor.value.getJSON();
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
        u: node.attrs.id.displayName,
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
