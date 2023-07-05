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
  chatNumber = 1;
  deleted = false;

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
    searchResults: "",
    altSearchResults: "",
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
        return meta.g === "mod" || meta.g === "mods" || meta.g === "help";
      } else {
        return meta.id === accountStore.state.accountId;
      }
    });

    if (isMentioned) {
      useSound(SOUNDS.MENTION).play();
    }
  }

  function searchUsername(username: string) {
    return api.moderation
      .searchUsername(username)
      .then((res) => {
        const data: { [key: number]: string } = res.data;
        state.searchResults = JSON.stringify(data);
        return Promise.resolve(res);
      })
      .catch((err) => {
        console.error(err);
        useToasts(err.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  function searchAltAccounts(accountId: number) {
    return api.moderation
      .searchAltAccouunts(accountId)
      .then((res) => {
        const data: { [key: number]: string } = res.data;
        state.altSearchResults = JSON.stringify(data);
        return Promise.resolve(res);
      })
      .catch((err) => {
        useToasts(err.message, {
          type: "error",
        });
        return Promise.reject(err);
      });
  }

  function ban(accountId: number) {
    useStomp().wsApi.moderation.ban(accountId);
  }

  function mute(accountId: number) {
    useStomp().wsApi.moderation.mute(accountId);
  }

  function rename(accountId: number, username: string) {
    useStomp().wsApi.moderation.rename(accountId, username);
  }

  function free(accountId: number) {
    useStomp().wsApi.moderation.free(accountId);
  }

  function mod(accountId: number) {
    useStomp().wsApi.moderation.mod(accountId);
  }

  return {
    state,
    getters,
    actions: {
      init,
      searchUsername,
      searchAltAccounts,
      ban,
      mute,
      rename,
      free,
      mod,
    },
  };
});
