import { defineStore } from "pinia";
import { useAPI } from "~/composables/useAPI";
import { useStomp } from "~/composables/useStomp";
import { useToasts } from "~/composables/useToasts";
import {
  isGroupMentionMeta,
  MentionMeta,
  Message,
  MessageData,
} from "~/store/entities/message";
import { useAccountStore } from "~/store/account";
import { SOUNDS, useSound } from "~/composables/useSound";

export type ChatLogMessageData = MessageData & {
  chatNumber: number;
  deleted: boolean;
};

export class ChatLogMessage extends Message implements ChatLogMessageData {
  chatNumber: number = 1;
  deleted: boolean = false;

  constructor(data: any) {
    super(data);
    Object.assign(this, data);
  }
}

export const useModerationStore = defineStore("moderation", () => {
  const api = useAPI();
  const stomp = useStomp();
  const accountStore = useAccountStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive({
    chatLog: <ChatLogMessage[]>[],
  });
  const getters = reactive({});

  async function init() {
    if (isInitialized.value) return Promise.resolve();
    return await getChatLog();
  }

  async function getChatLog() {
    return await api.moderation
      .getChatLog()
      .then((res) => {
        const data: ChatLogMessageData[] = res.data.messages;
        state.chatLog.length = 0;
        data.forEach((message) => {
          const msg = new ChatLogMessage(message);
          msg.setFlag("old");
          state.chatLog.unshift(msg);
        });

        stomp.addCallback(
          stomp.callbacks.onModChatEvent,
          "fair_chat_event",
          (body) => {
            addMessage(body);
          }
        );

        return Promise.resolve(res);
      })
      .catch((err) => {
        console.error(err);
        useToasts(err.response.data.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  function addMessage(body: ChatLogMessageData) {
    const msg = new ChatLogMessage(body);
    if (state.chatLog.length > 50) {
      state.chatLog.shift();
    }
    state.chatLog.push(msg);
    const isMentioned = msg.getMetadata().some((meta: MentionMeta) => {
      if (isGroupMentionMeta(meta)) {
        return meta.g === "mod" || meta.g === "mods";
      } else {
        return meta.id === accountStore.state.accountId;
      }
    });

    console.log(isMentioned, msg, msg.getMetadata());
    if (isMentioned) {
      useSound(SOUNDS.MENTION).play();
    }
  }

  return {
    state,
    getters,
    actions: {
      init,
    },
  };
});
