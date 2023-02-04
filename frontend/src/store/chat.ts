import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { useStomp } from "../composables/useStomp";
import { useAPI } from "../composables/useAPI";
import { MentionMeta, Message, MessageData } from "./entities/message";

export type ChatData = {
  messages: MessageData[];
  number: number;
};

export const useChatStore = defineStore("chat", () => {
  const api = useAPI();
  const stomp = useStomp();

  const isInitialized = ref<boolean>(false);
  const messages = reactive<Message[]>([]);
  const number = ref<number>(1);

  function init() {
    if (isInitialized.value) return;
    getChat(number.value);
  }

  function getChat(chatNumber: number) {
    isInitialized.value = true;
    api.chat
      .getChat(chatNumber)
      .then((response) => {
        const data: ChatData = response.data;
        Object.assign(messages, []);
        data.messages.forEach((message) => {
          const msg = new Message(message);
          msg.setFlag("old");
          messages.unshift(msg);
        });
        number.value = data.number;

        if (
          !stomp.callbacks.onChatEvent.some((x) => x.identifier === "default")
        ) {
          stomp.callbacks.onChatEvent.push({
            identifier: "default",
            callback: (body) => {
              if (messages.length > 50) {
                messages.shift();
              }
              messages.push(new Message(body));
            },
          });
        }
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function sendMessage(message: string, metadata: MentionMeta[]) {
    stomp.wsApi.chat.sendMessage(message, metadata, number.value);
  }

  function changeChat(newNumber: number) {
    stomp.wsApi.chat.changeChat(number.value, newNumber, true);
    getChat(newNumber);
  }

  return {
    // state
    messages,
    number,
    // actions
    init,
    sendMessage,
    changeChat,
  };
});
