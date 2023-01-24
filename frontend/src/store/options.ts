import { defineStore } from "pinia";
import { deepMerge } from "@antfu/utils";
import { useLocalStorage } from "@vueuse/core";

export const useOptionsStore = defineStore("options", () => {
  const state = useLocalStorage(
    "options",
    {
      general: {
        showAssholePoints: true,
        ladderSettings: {
          showETA: true,
        },
        boolean: true,
        string: "general",
      },
      account: {
        string: "account",
      },
    },
    {
      mergeDefaults: (storageValue, defaults) =>
        deepMerge(defaults, storageValue),
    }
  );

  return {
    state: state.value,
  };
});
