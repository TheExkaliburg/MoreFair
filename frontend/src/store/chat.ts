import { defineStore } from "pinia";
import { reactive, ref } from "vue";
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

export type ChatData = {
  messages: MessageData[];
  number: number;
};

export type ChatState = {
  messages: Message[];
  number: number;
};

export const useChatStore = defineStore("chat", () => {
  const api = useAPI();
  const stomp = useStomp();

  const accountStore = useAccountStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive<ChatState>({
    messages: <Message[]>[],
    number: 1,
  });
  const getters = reactive({});

  function init() {
    if (isInitialized.value) return;
    getChat(accountStore.state.highestCurrentLadder);
  }

  function reset() {
    isInitialized.value = false;
    init();
  }

  function getChat(chatNumber: number) {
    isInitialized.value = true;
    api.chat
      .getChat(chatNumber)
      .then((response) => {
        const data: ChatData = response.data;
        state.messages = [];
        data.messages.forEach((message) => {
          const msg = new Message(message);
          msg.setFlag("old");
          state.messages.unshift(msg);
        });

        state.number = data.number;

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

  function sendMessage(message: string, metadata: MentionMeta[]) {
    stomp.wsApi.chat.sendMessage(message, metadata, state.number);
  }

  function changeChat(newNumber: number) {
    stomp.wsApi.chat.changeChat(newNumber);
    getChat(newNumber);
  }

  function addMessage(body: OnChatEventBody) {
    const message = new Message(body);
    if (state.messages.length > 50) {
      state.messages.shift();
    }
    state.messages.push(message);

    // Find if one isn't a groupMention and has the id of the currentUser
    const isMentioned = message.getMetadata().some((meta) => {
      if (isGroupMentionMeta(meta)) {
        return false;
      }
      return meta.id === accountStore.state.accountId;
    });

    if (isMentioned) {
      useSound(SOUNDS.MENTION).play();
    }
  }

  function addSystemMessage(message: string, metadata: string = "[]") {
    addMessage({
      accountId: 1,
      username: "Chad",
      message,
      metadata,
      timestamp: Math.floor(Date.now() / 1000),
      tag: "🂮",
      assholePoints: 5950,
    });
  }

  function rename(accountId: number, username: string) {
    state.messages.forEach((message) => {
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
