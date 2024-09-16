import { defineStore } from "pinia";
import { computed, reactive } from "vue";
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
import { User } from "~/store/user";

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

export type NameChange = {
  accountId: number;
  displayName: string;
  timestamp: number;
};

export type UserEvent = {
  timestamp: number;
};

export enum SearchType {
  USERNAME = "USERNAME",
  ACCOUNT_ID = "ACCOUNT_ID",
}

export type ModerationState = {
  chatLog: Message[];
  usernameSearchInput: string;
  accountIdSearchInput: string;
  searchType: SearchType;
  searchResults: User[];
  nameChangeLog: NameChange[];
  userEventLog: UserEvent[];
};

export const useModerationStore = defineStore("moderation", () => {
  const api = useAPI();
  const stomp = useStomp();
  const accountStore = useAccountStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive<ModerationState>({
    chatLog: <Message[]>[],
    usernameSearchInput: "",
    accountIdSearchInput: "0",
    searchType: SearchType.USERNAME,
    searchResults: [],
    nameChangeLog: [],
    userEventLog: [],
  });
  const getters = reactive({
    allMessages: computed<Message[]>(() => {
      const result = [] as Message[];
      result.push(...(state.chatLog as Message[]));
      result.sort((a, b) => b.timestamp - a.timestamp);
      result.length = Math.min(result.length, 100);
      result.reverse();

      return result;
    }),
  });

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
          addMessage(msg);
        });

        stomp.addCallback(
          stomp.callbacks.onModChatEvent,
          "fair_chat_event",
          (body) => {
            addMessage(body);
          },
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
    const msg = new Message(body);
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

    if (isMentioned && !msg.hasFlag("old")) {
      useSound(SOUNDS.MENTION).play();
    }
  }

  function searchUsername(username: string) {
    return api.moderation
      .searchUsername(username)
      .then((res) => {
        const data: { users: User[] } = res.data;
        state.searchResults = data.users;
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
      .searchAltAccounts(accountId)
      .then((res) => {
        const data: { users: User[] } = res.data;
        state.searchResults = data.users;
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

  function search(type: SearchType) {
    state.searchType = type;
    if (type === SearchType.USERNAME) {
      searchUsername(state.usernameSearchInput).then();
    } else if (type === SearchType.ACCOUNT_ID) {
      const accountId = parseInt(state.accountIdSearchInput);
      if (isNaN(accountId)) return;

      searchAltAccounts(accountId).then();
    }
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
      search,
    },
  };
});
