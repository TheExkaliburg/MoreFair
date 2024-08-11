import { defineStore } from "pinia";
import Decimal from "break_infinity.js";
import { RemovableRef } from "@vueuse/core";
import { useLocalStorage } from "~/composables/useLocalStorage";
import { useLadderStore } from "~/store/ladder";
import { useAccountStore } from "~/store/account";
import { useAPI } from "~/composables/useAPI";

export enum VinegarSuccessType {
  SHIELDED = "SHIELDED",
  SHIELD_DEFENDED = "SHIELD_DEFENDED",
  DEFENDED = "DEFENDED",
  SUCCESS = "SUCCESS",
  DOUBLE_SUCCESS = "DOUBLE_SUCCESS",
}

export type VinegarThrow = {
  accountId: number;
  targetId: number;
  timestamp: number;
  ladderNumber: number;
  roundNumber: number;
  vinegarThrown: Decimal;
  percentage: number;
  successType: VinegarSuccessType;
};

export type GrapesState = {
  throwRecords: VinegarThrow[];
};

export type GrapesStorage = {
  vinegarThrowPercentage: number;
};

const defaultValues: GrapesStorage = {
  vinegarThrowPercentage: 100,
};

const storage: RemovableRef<GrapesStorage> = useLocalStorage(
  "grapes",
  defaultValues,
);

export const useGrapesStore = defineStore("grapes", () => {
  const ladderStore = useLadderStore();
  const accountStore = useAccountStore();

  const api = useAPI();

  const startedInitialization = ref<boolean>(false);
  const state = reactive<GrapesState>({
    throwRecords: [],
  });

  const getters = reactive({
    selectedVinegar: computed<Decimal>(() => {
      const vinegar = ladderStore.getters.yourRanker?.vinegar ?? new Decimal(0);
      return vinegar.mul(storage.value.vinegarThrowPercentage / 100);
    }),
  });

  init().then();

  async function init() {
    if (startedInitialization.value) return;
    startedInitialization.value = true;
    return await getVinegarRecords();
  }

  async function reset() {
    startedInitialization.value = false;
    return await init();
  }

  async function getVinegarRecords() {
    startedInitialization.value = true;
    return await api.grapes
      .getVinegarRecords()
      .then((response) => {
        const data: GrapesState = response.data;
        state.throwRecords.length = 0;
        data.throwRecords.forEach((v) => {
          return state.throwRecords.push({
            ...v,
            vinegarThrown: new Decimal(v.vinegarThrown),
          });
        });
      })
      .catch((_) => {
        startedInitialization.value = false;
      });
  }

  async function setVinegarSplit(split: number) {
    const settings = accountStore.state.settings;
    return await accountStore.actions.saveSettings({
      ...settings,
      vinegarSplit: split,
    });
  }

  return {
    storage,
    state,
    getters,
    actions: {
      init,
      reset,
      setVinegarSplit,
    },
  };
});
