import { defineStore } from "pinia";

export const useUiStore = defineStore("ui", {
  state: () => ({
    sidebarExpanded: false,
  }),
  getters: {},
  actions: {},
});
