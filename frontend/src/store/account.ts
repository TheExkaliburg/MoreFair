import { defineStore } from "pinia";
import { reactive } from "vue";
import { useAPI } from "~/composables/useAPI";

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
};

export const useAccountStore = defineStore("account", () => {
  const api = useAPI();

  const isInitialized = ref<boolean>(false);
  const state = reactive({
    accessRole: AccessRole.PLAYER,
    accountId: 1,
    highestCurrentLadder: 1,
  });

  function init() {
    if (isInitialized.value) return;
    getAccountDetails();
  }

  function getAccountDetails() {
    api.account.getAccountDetails().then((res) => {
      const data: AccountData = res.data;
      console.log(data);
      state.accessRole = data.accessRole;
      state.accountId = data.accountId;
      state.highestCurrentLadder = data.highestCurrentLadder;
    });
  }

  return {
    state,
    init,
  };
});
