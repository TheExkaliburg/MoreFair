import { defineStore } from "pinia";
import { watch } from "vue";

export const useUiStore = defineStore("ui", () => {
  // variables
  const sidebarExpanded = ref<Boolean>(false);
  const ladderEnabled = ref<Boolean>(true);
  const chatEnabled = ref<Boolean>(true);

  // side-effects
  watch(ladderEnabled, (value: Boolean) => {
    if (!value && !chatEnabled.value) {
      chatEnabled.value = true;
    }
  });
  watch(chatEnabled, (value: Boolean) => {
    if (!value && !ladderEnabled.value) {
      ladderEnabled.value = true;
    }
  });

  return {
    sidebarExpanded,
    ladderEnabled,
    chatEnabled,
  };
});
