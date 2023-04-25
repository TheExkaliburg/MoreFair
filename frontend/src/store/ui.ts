import { defineStore } from "pinia";
import { reactive, watch } from "vue";

export const useUiStore = defineStore("ui", () => {
  // variables
  const state = reactive({
    sidebarExpanded: false,
    ladderEnabled: true,
    chatEnabled: true,
  });

  // actions
  function toggleSidebar() {
    state.sidebarExpanded = !state.sidebarExpanded;
  }

  // side-effects
  watch(
    () => state.ladderEnabled,
    (value: boolean) => {
      if (!value && !state.chatEnabled) {
        state.chatEnabled = true;
      }
    }
  );
  watch(
    () => state.chatEnabled,
    (value: boolean) => {
      if (!value && !state.ladderEnabled) {
        state.ladderEnabled = true;
      }
    }
  );

  return {
    state,
    toggleSidebar,
  };
});
