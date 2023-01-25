import { defineStore } from "pinia";
import { ref, watch } from "vue";

export const useUiStore = defineStore("ui", () => {
  // variables
  const sidebarExpanded = ref<boolean>(false);
  const ladderEnabled = ref<boolean>(true);
  const chatEnabled = ref<boolean>(true);

  // actions
  function toggleSidebar() {
    sidebarExpanded.value = !sidebarExpanded.value;
  }

  // side-effects
  watch(ladderEnabled, (value: boolean) => {
    if (!value && !chatEnabled.value) {
      chatEnabled.value = true;
    }
  });
  watch(chatEnabled, (value: boolean) => {
    if (!value && !ladderEnabled.value) {
      ladderEnabled.value = true;
    }
  });

  return {
    sidebarExpanded,
    ladderEnabled,
    chatEnabled,
    toggleSidebar,
  };
});
