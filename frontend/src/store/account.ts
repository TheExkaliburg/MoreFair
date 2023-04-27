import { defineStore } from "pinia";
import { computed, reactive } from "vue";
import { useAPI } from "~/composables/useAPI";
import { OnAccountEventBody, useStomp } from "~/composables/useStomp";
import { useLadderStore } from "~/store/ladder";
import { useChatStore } from "~/store/chat";
import { useAuthStore } from "~/store/authentication";

export enum AccessRole {
  OWNER,
  MODERATOR,
  PLAYER,
  MUTED_PLAYER,
  BANNED_PLAYER,
  BROADCASTER,
}

export enum AccountEventType {
  BAN = "BAN",
  FREE = "FREE",
  MUTE = "MUTE",
  MOD = "MOD",
  NAME_CHANGE = "NAME_CHANGE",
  INCREASE_HIGHEST_LADDER = "INCREASE_HIGHEST_LADDER",
}

export type AccountData = {
  accessRole: AccessRole;
  accountId: number;
  username: string;
  email: string;
  highestCurrentLadder: number;
  uuid: string;
};

export const useAccountStore = defineStore("account", () => {
  const api = useAPI();

  const ladderStore = useLadderStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive<AccountData>({
    accessRole: AccessRole.PLAYER,
    accountId: 1,
    username: "Mystery Guest",
    email: "",
    highestCurrentLadder: 1,
    uuid: "",
  });
  const getters = reactive({
    isGuest: computed<boolean>(() => useAuthStore().getters.isGuest),
  });

  function init() {
    if (isInitialized.value) return;
    getAccountDetails();
  }

  function reset() {
    isInitialized.value = false;
    init();
  }

  function getAccountDetails() {
    api.account.getAccountDetails().then((res) => {
      const data: AccountData = res.data;
      state.accessRole = data.accessRole;
      state.accountId = data.accountId;
      state.highestCurrentLadder = data.highestCurrentLadder;
      state.uuid = data.uuid;
      const stomp = useStomp();
      stomp.connectPrivateChannel(state.uuid);
      stomp.addCallback(
        stomp.callbacks.onAccountEvent,
        "fair_account_events",
        handleAccountEvents
      );
    });
  }

  function handleAccountEvents(body: OnAccountEventBody) {
    const event = body.eventType;
    const isYou = state.accountId === body.accountId;
    let ranker;
    if (isYou) {
      ranker = ladderStore.getters.yourRanker;
    } else {
      ranker = ladderStore.state.rankers.find(
        (r) => r.accountId === body.accountId
      );
    }

    if (ranker === undefined) return;

    switch (event) {
      case AccountEventType.NAME_CHANGE:
        ranker.username = body.data;
        useChatStore().actions.rename(body.accountId, body.data);
        break;
      case AccountEventType.MOD:
        state.accessRole = AccessRole.MODERATOR;
        break;
      case AccountEventType.FREE:
        state.accessRole = AccessRole.PLAYER;
        break;
      case AccountEventType.MUTE:
        state.accessRole = AccessRole.MUTED_PLAYER;
        break;
      case AccountEventType.BAN:
        state.accessRole = AccessRole.BANNED_PLAYER;
        break;
      case AccountEventType.INCREASE_HIGHEST_LADDER:
        state.highestCurrentLadder = body.data;
        break;
      default:
        console.error("Unknown account event type: " + event);
        break;
    }
  }

  return {
    state,
    getters,
    actions: {
      init,
      reset,
    },
  };
});
