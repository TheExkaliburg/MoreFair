import { defineStore } from "pinia";
import { reactive } from "vue";
import { useAPI } from "~/composables/useAPI";
import {
  RoundSettings,
  RoundSettingsData,
} from "~/store/entities/roundSettings";

export enum RoundType {
  DEFAULT,
  FAST,
  SLOW,
  AUTO,
  CHAOS,
}

export type RoundData = {
  settings: RoundSettingsData;
  assholeLadder: number;
  autoPromoteLadder: number;
  types: RoundType[];
};

export const useRoundStore = defineStore("round", () => {
  const api = useAPI();

  const isInitialized = ref<boolean>(false);
  const state = reactive({
    assholeLadder: 20,
    autoPromoteLadder: 1,
    types: new Set([RoundType.DEFAULT]),
    settings: new RoundSettings({}),
  });
  const getters = reactive({});

  function init() {
    if (isInitialized.value) return;
    getCurrentRound();
  }

  function getCurrentRound() {
    isInitialized.value = true;
    api.round
      .getCurrentRound()
      .then((res) => {
        const data: RoundData = res.data;
        state.types = new Set();
        data.types.forEach((t) => state.types.add(t));
        state.assholeLadder = data.assholeLadder;
        state.autoPromoteLadder = data.autoPromoteLadder;
        state.settings = new RoundSettings(data.settings);
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  return {
    state,
    getters,
    // actions
    actions: {
      init,
    },
  };
});
