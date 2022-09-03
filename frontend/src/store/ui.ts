import { defineStore } from "pinia";

export const useUiStore = defineStore("ui", {
  state: () => ({
    sidebarExpanded: false,
    ladderEnabled: true,
    chatEnabled: true,
  }),
  getters: {},
  actions: {},
});
