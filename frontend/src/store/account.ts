import { defineStore } from "pinia";
import { reactive } from "vue";
import { useAPI } from "~/composables/useAPI";
import { useStomp } from "~/composables/useStomp";

export enum AccessRole {
  OWNER,
  MODERATOR,
  PLAYER,
  MUTED_PLAYER,
  BANNED_PLAYER,
  BROADCASTER,
}

export type AccountData = {
  accessRole: AccessRole.PLAYER;
  accountId: number;
  highestCurrentLadder: number;
  uuid: string;
};

export const useAccountStore = defineStore("account", () => {
  const api = useAPI();

  const isInitialized = ref<boolean>(false);
  const state = reactive({
    accessRole: AccessRole.PLAYER,
    accountId: 1,
    highestCurrentLadder: 1,
    uuid: "",
  });
  const getters = reactive({});

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
      useStomp().connectPrivateChannel(state.uuid);
    });
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
