import { defineStore } from "pinia";
import { reactive } from "vue";
import Decimal from "break_infinity.js";
import { Ranker, RankerData } from "~/store/entities/ranker";

export enum LadderType {
  DEFAULT,
  TINY,
  SMALL,
  BIG,
  GIGANTIC,
  FREE_AUTO,
  NO_AUTO,
  ASSHOLE,
}

export type LadderData = {
  rankers: RankerData[];
  number: number;
  types: LadderType[];
  basePointsToPromote: string;
};

export const useLadderStore = defineStore("ladder", () => {
  const api = useAPI();
  const stomp = useStomp();

  const isInitialized = ref<boolean>(false);
  const rankers = reactive<Ranker[]>([]);
  const number = ref<number>(1);
  const types = reactive<Set<LadderType>>(new Set());
  const basePointsToPromote = ref<Decimal>(new Decimal(0));

  function init() {
    if (isInitialized.value) return;
    getLadder(number.value);
  }

  function getLadder(ladderNumber: number) {
    isInitialized.value = true;
    api.ladder
      .getLadder(ladderNumber)
      .then((res) => {
        const data: LadderData = res.data;
        Object.assign(rankers, []);
        data.rankers.forEach((ranker) => {
          rankers.push(new Ranker(ranker));
        });
        Object.assign(types, new Set());
        data.types.forEach((s) => {
          types.add(s);
        });
        number.value = data.number;
        basePointsToPromote.value = new Decimal(data.basePointsToPromote);

        if (
          !stomp.callbacks.onLadderEvent.some((x) => x.identifier === "default")
        ) {
          stomp.callbacks.onLadderEvent.push({
            identifier: "default",
            callback: (_) => {},
          });
        }

        if (!stomp.callbacks.onTick.some((x) => x.identifier === "default")) {
          stomp.callbacks.onTick.push({
            identifier: "default",
            callback: (_) => {},
          });
        }
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function changeLadder(newNumber: number) {
    stomp.wsApi.ladder.changeLadder(number.value, newNumber, true);
    getLadder(newNumber);
  }

  return {
    // state
    rankers,
    number,
    types,
    basePointsToPromote,
    // actions
    init,
    changeLadder,
  };
});
