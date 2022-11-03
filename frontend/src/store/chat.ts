import { defineStore } from "pinia";
import { reactive } from "vue";
import { MentionMeta, Message } from "~/store/entities/message";
import { useStomp } from "~/composables/useStomp";
import { useAPI } from "~/composables/useAPI";

export type ChatData = {
  messages: Message[];
  number: number;
};

export const useChatStore = defineStore("chat", () => {
  const api = useAPI();
  const stomp = useStomp();

  const messages = reactive<Message[]>([]);
  const number = ref<number>(1);

  api.chat.getChat(number.value).then((response) => {
    const data: ChatData = response.data;
    Object.assign(messages, data.messages);
    number.value = data.number;
  });

  function sendMessage(message: string, metadata: MentionMeta[]) {
    stomp.wsApi.chat.sendMessage(message, metadata, number.value);
  }

  return {
    // vars
    messages,
    number,
    // actions
    sendMessage,
  };
});
