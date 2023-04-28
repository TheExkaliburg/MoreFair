import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { MentionMeta, Message, MessageData } from "./entities/message";
import { OnChatEventBody, useStomp } from "~/composables/useStomp";
import { useAPI } from "~/composables/useAPI";

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

  const isInitialized = ref<boolean>(false);
  const state = reactive<ChatState>({
    messages: <Message[]>[],
    number: 1,
  });
  const getters = reactive({});

  function init() {
    if (isInitialized.value) return;
    getChat(state.number);
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

        stomp.wsApi.chat.changeChat(state.number, data.number);
        state.number = data.number;

        stomp.addCallback(
          stomp.callbacks.onChatEvent,
          "fair_chat_event",
          (body) => addLocalMessage(body)
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
    stomp.wsApi.chat.changeChat(state.number, newNumber, true);
    getChat(newNumber);
  }

  function addLocalMessage(body: OnChatEventBody) {
    if (state.messages.length > 50) {
      state.messages.shift();
    }
    state.messages.push(new Message(body));
  }

  function addSystemMessage(message: string, metadata: string = "[]") {
    addLocalMessage({
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
      addLocalMessage,
      addSystemMessage,
      rename,
    },
  };
});
