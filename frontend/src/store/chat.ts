import { defineStore } from "pinia";
import { reactive, ref } from "vue";
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
}

export type ChatData = {
  type: ChatType;
  messages: MessageData[];
  number: number;
};

export type ChatState = {
  ladderMessages: Message[];
  globalMessages: Message[];
  systemMessages: Message[];
  ladderChatNumber: number;
  input: JSONContent;
  selectedChatType: ChatType;
  ignoredChatTypes: Set<ChatType>;
};

export const useChatStore = defineStore("chat", () => {
  const api = useAPI();
  const stomp = useStomp();

  const accountStore = useAccountStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive<ChatState>({
    ladderMessages: <Message[]>[],
    globalMessages: <Message[]>[],
    systemMessages: <Message[]>[],
    ladderChatNumber: 1,
    input: { type: "doc", content: [{ type: "paragraph" }] },
    selectedChatType: ChatType.GLOBAL,
    ignoredChatTypes: new Set(),
  });
  const getters = reactive({});

  function init() {
    if (isInitialized.value) return;
    getChat(ChatType.GLOBAL);
    getChat(ChatType.LADDER, accountStore.state.highestCurrentLadder);
    getChat(ChatType.SYSTEM);
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

        switch (data.type) {
          case ChatType.GLOBAL:
            state.globalMessages.length = 0;
            break;
          case ChatType.LADDER:
            state.ladderMessages.length = 0;
            state.ladderChatNumber = data.number;
            break;
          case ChatType.SYSTEM:
            state.systemMessages.length = 0;
            break;
        }

        data.messages.forEach((message) => {
          const msg = new Message(message);
          msg.setFlag("old");
          addMessage(msg);
        });

        state.ladderChatNumber = data.number;

        stomp.addCallback(
          stomp.callbacks.onChatEvent,
          "fair_chat_event",
          (body) => addMessage(body)
        );
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function sendMessage(
    message: string,
    metadata: MentionMeta[],
    chatType: ChatType
  ) {
    if (chatType === ChatType.LADDER)
      stomp.wsApi.chat.sendMessage(
        message,
        metadata,
        chatType,
        state.ladderChatNumber
      );
    else stomp.wsApi.chat.sendMessage(message, metadata, chatType);
  }

  function changeChat(newNumber: number) {
    stomp.wsApi.chat.changeLadderChat(newNumber);
    getChat(ChatType.LADDER, newNumber);
  }

  function addMessage(body: OnChatEventBody): void {
    const message = new Message(body);
    if (state.ladderMessages.length > 50) {
      state.ladderMessages.shift();
    }
    state.ladderMessages.push(message);

    // Find if one isn't a groupMention and has the id of the currentUser
    const isMentioned = message.getMetadata().some((meta) => {
      if (isGroupMentionMeta(meta)) {
        return useOptionsStore().state.chat.subscribedMentions.value.includes(
          meta.g
        );
      }
      return meta.id === accountStore.state.accountId;
    });

    if (isMentioned) {
      useSound(SOUNDS.MENTION).play();
    }
  }

  function addSystemMessage(message: string, metadata = "[]") {
    addMessage({
      accountId: 1,
      username: "Chad",
      message,
      metadata,
      timestamp: Math.floor(Date.now() / 1000),
      tag: "🂮",
      assholePoints: 5950,
      isMod: false,
      chatType: ChatType.SYSTEM,
    });
  }

  function rename(accountId: number, username: string) {
    state.ladderMessages.forEach((message) => {
      if (message.accountId === accountId) {
        message.username = username;
      }
      /* Since we don't update in the backend, mentions stay the way they are, but this would be how we update them in the frontend
      const metadata = message.getMetadata();
      metadata.forEach((meta) => {
        if (!isGroupMentionMeta(meta) && meta.id === accountId) {
          meta.u = username;
        }
      });
      message.metadata = JSON.stringify(metadata);
       */
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
    },
  };
});
