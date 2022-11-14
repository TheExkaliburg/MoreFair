import { defineStore } from "pinia";
import { reactive } from "vue";
import { MentionMeta, Message } from "~/store/entities/message";
import { useStomp } from "~/composables/useStomp";
import { useAPI } from "~/composables/useAPI";

export type ChatData = {
  messages: object[];
  number: number;
};

export const useChatStore = defineStore("chat", () => {
  const api = useAPI();
  const stomp = useStomp();

  const messages = reactive<Message[]>([]);
  const number = ref<number>(1);

  getChat(number.value);

  function getChat(chatNumber: number) {
    api.chat.getChat(chatNumber).then((response) => {
      const data: ChatData = response.data;
      Object.assign(messages, []);
      data.messages.forEach((message: Message) => {
        messages.unshift(new Message(message));
      });
      number.value = data.number;

      if (
        !stomp.callbacks.onChatEvent.some((x) => x.identifier === "default")
      ) {
        stomp.callbacks.onChatEvent.push({
          identifier: "default",
          callback: (body) => {
            console.log(body);
            messages.push(new Message(body));
          },
        });
      }
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
    // vars
    messages,
    number,
    // actions
    sendMessage,
    changeChat,
  };
});
