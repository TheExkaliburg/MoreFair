import { defineStore } from "pinia";
import { computed, reactive, ref } from "vue";
import { JSONContent } from "@tiptap/core";
import {
  isGroupMentionMeta,
  MentionMeta,
  Message,
  MessageData,
} from "./entities/message";
import { OnChatEventBody, useStomp } from "~/composables/useStomp";
import { useAPI } from "~/composables/useAPI";
import { useAccountStore } from "~/store/account";
import { SOUNDS, useSound } from "~/composables/useSound";
import { useOptionsStore } from "~/store/options";

export enum ChatType {
  GLOBAL = "GLOBAL",
  LADDER = "LADDER",
  SYSTEM = "SYSTEM",
  MOD = "MOD",
}

export type Suggestion = { accountId: number; displayName: string };

export type ChatData = {
  type: ChatType;
  messages: MessageData[];
  number: number;
};

export type ChatState = {
  messages: Map<ChatType, Message[]>;
  input: JSONContent;
  selectedChatType: ChatType;
  ignoredChatTypes: Set<ChatType>;
  suggestions: Suggestion[];
};

export const useChatStore = defineStore("chat", () => {
  const api = useAPI();
  const stomp = useStomp();

  const accountStore = useAccountStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive<ChatState>({
    messages: new Map<ChatType, Message[]>(),
    input: { type: "doc", content: [{ type: "paragraph" }] },
    selectedChatType: ChatType.GLOBAL,
    ignoredChatTypes: new Set(),
    suggestions: [],
  });
  const keys = Object.keys(ChatType) as ChatType[];
  keys.forEach((key) => {
    state.messages.set(key, []);
  });

  const getters = reactive({
    allMessages: computed<Message[]>(() => {
      const keys = Object.keys(ChatType) as ChatType[];
      const result: Message[] = [];

      keys.forEach((key) => {
        const messages = state.messages.get(key) ?? [];
        if (!state.ignoredChatTypes.has(key)) {
          result.push(...messages);
        }
      });

      result.sort((a, b) => b.timestamp - a.timestamp);
      result.length = Math.min(result.length, 50);
      result.reverse();

      return result;
    }),
  });

  function init() {
    if (isInitialized.value) return;
    getChat(ChatType.LADDER, accountStore.state.highestCurrentLadder);
    getChat(ChatType.GLOBAL);
    getChat(ChatType.SYSTEM);
    getChat(ChatType.MOD);
    getSuggestions();
  }

  function reset() {
    isInitialized.value = false;
    init();
  }

  function getChat(chatType: ChatType, chatNumber?: number) {
    isInitialized.value = true;
    api.chat
      .getChat(chatType, chatNumber)
      .then((response) => {
        const data: ChatData = response.data;

        const messages = state.messages.get(data.type);
        if (messages === undefined) {
          return;
        }

        messages.length = 0;

        data.messages.sort((a, b) => a.timestamp - b.timestamp);
        data.messages.forEach((message) => {
          const msg = new Message(message);
          msg.setFlag("old");
          addMessage(msg);
        });

        stomp.addCallback(
          stomp.callbacks.onChatEvent,
          "fair_chat_event",
          (body) => addMessage(body),
        );
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function getSuggestions() {
    api.chat.getSuggestions().then((response) => {
      if (response.status === 200) {
        state.suggestions = response.data;
      }
    });
  }

  function sendMessage(message: string, metadata: MentionMeta[]) {
    if (state.selectedChatType === ChatType.LADDER) {
      stomp.wsApi.chat.sendMessage(
        message,
        metadata,
        state.selectedChatType,
        useAccountStore().state.highestCurrentLadder,
      );
    } else {
      stomp.wsApi.chat.sendMessage(message, metadata, state.selectedChatType);
    }
  }

  function changeChat(newNumber: number) {
    stomp.wsApi.chat.changeLadderChat(newNumber);
    getChat(ChatType.LADDER, newNumber);
  }

  function ignorable(msg: Message): boolean {
    return (
      msg.chatType !== ChatType.MOD &&
      msg.chatType !== ChatType.SYSTEM &&
      msg.accountId !== useAccountStore().state.accountId
    );
  }

  function addMessage(body: OnChatEventBody): void {
    const message = new Message(body);
    const messages = state.messages.get(message.chatType);
    if (messages === undefined) {
      return;
    }

    if (
      ignorable(message) &&
      useOptionsStore().state.chat.ignoredPlayers.value.includes(
        message.accountId.toString(),
      )
    ) {
      return;
    }

    if (messages.length > 50) {
      messages.shift();
    }
    messages.push(message);

    // Find if one isn't a groupMention and has the id of the currentUser
    const isMentioned = message.getMetadata().some((meta) => {
      if (isGroupMentionMeta(meta)) {
        return useOptionsStore().state.chat.subscribedMentions.value.includes(
          meta.g,
        );
      }
      return meta.id === accountStore.state.accountId;
    });

    if (isMentioned && !message.hasFlag("old")) {
      useSound(SOUNDS.MENTION).play();
    }
  }

  function addSystemMessage(message: string, metadata = "[]") {
    addMessage({
      accountId: 1,
      username: "Chad",
      ladderNumber: 0,
      message,
      metadata,
      timestamp: Math.floor(Date.now() / 1000),
      tag: "🂮",
      assholePoints: 5950,
      isMod: false,
      chatType: ChatType.SYSTEM,
    });
  }

  function clearMessages(accountId: number) {
    state.messages.forEach((messages, type) => {
      const clearedMessages = messages.filter((m) => m.accountId !== accountId);
      state.messages.set(type, clearedMessages);
    });
  }

  function rename(accountId: number, username: string) {
    state.messages.forEach((messages) => {
      messages.forEach((message) => {
        if (message.accountId === accountId) {
          message.username = username;
        }
        // Since we don't update mentions in the backend, they stay the way they are
      });
    });
    state.suggestions.forEach((suggestion) => {
      if (suggestion.accountId === accountId) {
        suggestion.displayName = username;
      }
    });
  }

  return {
    state,
    getters,
    actions: {
      init,
      reset,
      sendMessage,
      changeChat,
      addLocalMessage: addMessage,
      addSystemMessage,
      rename,
      clearMessages,
    },
  };
});
