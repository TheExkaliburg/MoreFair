import { defineStore } from "pinia";
import { reactive } from "vue";
import { useAccountStore } from "~/store/account.ts";

export type ModViewState = {
  selectedPlayerId: number | null;
  currentView: ModViewTabType;
  overviewInformation: InspectorOverviewInformation | null;
};

export enum ModViewTabType {
  Overview = "OVERVIEW",
  Messages = "MESSAGE_LOG",
  EventLog = "EVENT_LOG",
  AuditLog = "AUDIT_LOG",
  NameHistory = "NAME_HISTORY",
  AltAccounts = "ALT_ACCOUNTS",
}

export type InspectorOverviewInformation = {
  displayName: string;
  messageLogCount: number;
  eventLogCount: number;
  auditLogCount: number;
  nameHistoryCount: number;
  altAccountsCount: number;
};

export const useModViewStore = defineStore("modView", () => {
  const account = useAccountStore();
  const api = useAPI();

  const state = reactive<ModViewState>({
    selectedPlayerId: null,
    currentView: ModViewTabType.Overview,
    overviewInformation: null,
  });

  const getters = reactive({
    isOpen: computed<boolean>(
      () => state.selectedPlayerId !== null && account.getters.isMod,
    ),
  });

  watch(
    () => state.selectedPlayerId,
    () => {
      fetchOverviewInformation();
    },
  );

  function open(accountId: number) {
    state.selectedPlayerId = accountId;
    state.currentView = ModViewTabType.Overview;
  }

  function close() {
    state.selectedPlayerId = null;
  }

  function reload() {
    if (state.currentView === ModViewTabType.Overview) {
      fetchOverviewInformation();
    }
  }

  function changeView(type: ModViewTabType) {
    state.currentView = type;
  }

  function fetchOverviewInformation() {
    if (state.selectedPlayerId === null) {
      return;
    }

    api.moderation
      .getInspectorInformation(state.selectedPlayerId)
      .then((response) => {
        state.overviewInformation = response.data;
      });
  }

  return {
    state,
    getters,
    actions: {
      open,
      close,
      reload,
      changeView,
    },
  };
});
