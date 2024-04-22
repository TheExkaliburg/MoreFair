import { defineStore } from "pinia";
import Decimal from "break_infinity.js";
import { useLocalStorage } from "~/composables/useLocalStorage";
import { useLadderStore } from "~/store/ladder";

const defaultValues = {
  vinegarThrowPercentage: 100,
};

const storage = useLocalStorage("grapes", defaultValues);

export const useGrapesStore = defineStore("grapes", () => {
  const ladderStore = useLadderStore();

  const state = storage;

  const getters = reactive({
    selectedVinegar: computed<Decimal>(() => {
      const vinegar = ladderStore.getters.yourRanker?.vinegar ?? new Decimal(0);
      return vinegar.mul(state.value.vinegarThrowPercentage / 100);
    }),
  });

  return {
    state,
    getters,
  };
});
