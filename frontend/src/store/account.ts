import { defineStore } from "pinia";
import { computed, reactive } from "vue";
import { useAPI } from "~/composables/useAPI";
import { OnAccountEventBody, useStomp } from "~/composables/useStomp";
import { useLadderStore } from "~/store/ladder";
import { useChatStore } from "~/store/chat";
import { useAuthStore } from "~/store/authentication";
import { SOUNDS, useSound } from "~/composables/useSound";
import { useToasts } from "~/composables/useToasts";
import { useModerationStore } from "~/store/moderation";

export enum AccessRole {
  OWNER = "OWNER",
  MODERATOR = "MODERATOR",
  PLAYER = "PLAYER",
  MUTED_PLAYER = "MUTED_PLAYER",
  BANNED_PLAYER = "BANNED_PLAYER",
  BROADCASTER = "BROADCASTER",
}

export enum AccountEventType {
  BAN = "BAN",
  FREE = "FREE",
  MUTE = "MUTE",
  MOD = "MOD",
  NAME_CHANGE = "NAME_CHANGE",
  INCREASE_HIGHEST_LADDER = "INCREASE_HIGHEST_LADDER",
}

export type AccountSettings = {
  vinegarSplit: number;
};

export type AccountData = {
  accessRole: AccessRole;
  accountId: number;
  username: string;
  email: string;
  highestCurrentLadder: number;
  uuid: string;
  settings: AccountSettings;
};

export const useAccountStore = defineStore("account", () => {
  const api = useAPI();
  const stomp = useStomp();

  const isInitialized = ref<boolean>(false);

  const state = reactive<AccountData>({
    accessRole: AccessRole.PLAYER,
    accountId: 1,
    username: "Mystery Guest",
    email: "",
    highestCurrentLadder: 1,
    uuid: "",
    settings: {
      vinegarSplit: 50,
    },
  });
  const getters = reactive({
    isGuest: computed<boolean>(() => useAuthStore().getters.isGuest),
    isMod: computed<boolean>(() => {
      return (
        state.accessRole === AccessRole.MODERATOR ||
        state.accessRole === AccessRole.OWNER
      );
    }),
  });

  init().then();

  async function init() {
    if (isInitialized.value) return Promise.resolve();
    return await getAccountDetails();
  }

  async function reset() {
    isInitialized.value = false;
    return await init();
  }

  async function getAccountDetails() {
    isInitialized.value = true;
    return await api.account
      .getAccountDetails()
      .then((res) => {
        const data: AccountData = res.data;
        state.accessRole = data.accessRole;
        state.accountId = data.accountId;
        state.highestCurrentLadder = data.highestCurrentLadder;
        state.uuid = data.uuid;
        state.username = data.username;
        state.email = data.email;
        state.settings = data.settings;
        stomp.connectPrivateChannel(state.uuid);
        stomp.addCallback(
          stomp.callbacks.onAccountEvent,
          "fair_account_events",
          handleAccountEvents,
        );

        if (getters.isMod) {
          useModerationStore().actions.init();
        }

        useLadderStore().actions.init();
        useChatStore().actions.init();
        return Promise.resolve(res);
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function handleAccountEvents(body: OnAccountEventBody) {
    const event = body.eventType;
    const isYou = state.accountId === body.accountId;
    let ranker;
    if (isYou) {
      ranker = useLadderStore().getters.yourRanker;
    } else {
      ranker = useLadderStore().state.rankers.find(
        (r) => r.accountId === body.accountId,
      );
    }

    switch (event) {
      case AccountEventType.NAME_CHANGE:
        if (isYou) state.username = body.data;
        if (ranker) ranker.username = body.data;
        useChatStore().actions.rename(body.accountId, body.data);
        break;
      case AccountEventType.MOD:
        if (isYou) state.accessRole = AccessRole.MODERATOR;
        break;
      case AccountEventType.FREE:
        if (isYou) state.accessRole = AccessRole.PLAYER;
        break;
      case AccountEventType.MUTE:
        if (isYou) state.accessRole = AccessRole.MUTED_PLAYER;
        useChatStore().actions.clearMessages(body.accountId);
        break;
      case AccountEventType.BAN:
        if (isYou) state.accessRole = AccessRole.BANNED_PLAYER;
        useChatStore().actions.clearMessages(body.accountId);
        break;
      case AccountEventType.INCREASE_HIGHEST_LADDER:
        useSound(SOUNDS.PROMOTION).play();
        state.highestCurrentLadder = body.data;
        useChatStore().actions.changeChat(state.highestCurrentLadder);
        break;
      default:
        console.error("Unknown account event type: " + event);
        break;
    }
  }

  async function changeDisplayName(name: string) {
    return await api.account
      .changeDisplayName(name)
      .then((res) => {
        state.username = res.data.displayName;
        return Promise.resolve(res);
      })
      .catch((err) => {
        useToasts(err.response.data.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  async function saveSettings(settings: AccountSettings) {
    return await api.account
      .saveSettings(settings)
      .then((result) => {
        state.settings = result.data;
        return Promise.resolve(result);
      })
      .catch((err) => {
        useToasts(err.response.data.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  return {
    state,
    getters,
    actions: {
      init,
      reset,
      changeDisplayName,
      saveSettings,
    },
  };
});
