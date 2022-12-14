import { defineStore } from "pinia";
import { reactive } from "vue";
import Decimal from "break_infinity.js";
import { Ranker, RankerData } from "~/store/entities/ranker";
import { OnTickBody } from "~/composables/useStomp";

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

  const state = reactive({
    isInitialized: false,
    rankers: [] as Ranker[],
    number: 1,
    types: new Set() as Set<LadderType>,
    basePointsToPromote: new Decimal(0),
  });
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
          state.rankers.push(new Ranker(ranker));
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
            callback: (body: OnTickBody) => {
              calculateTick(body.delta);
            },
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

  function calculateTick(deltaSeconds: number) {
    const delta = new Decimal(deltaSeconds);
    const newRankers: Ranker[] = [...state.rankers];
    newRankers.sort((a, b) => b.points.cmp(a.points));

    for (let i = 0; i < newRankers.length; i++) {
      const ranker = new Ranker(newRankers[i]);
      newRankers[i] = ranker;
      newRankers[i].rank = i + 1;

      // If ranker still on ladder
      if (ranker.growing) {
        // Power & Points
        if (ranker.rank !== 1) {
          ranker.power = ranker.power.add(
            new Decimal((ranker.bias + ranker.rank + 1) * ranker.multi)
              .mul(delta)
              .floor()
          );
        }
        ranker.points = ranker.points.add(ranker.power.mul(delta).floor());

        // TODO: Vinegar & Grapes
        newRankers[i] = ranker;

        for (let j = i - 1; j >= 0; j--) {
          const currentRanker = newRankers[j + 1];
          if (currentRanker.points.cmp(newRankers[j].points) > 0) {
            // Move 1 position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            newRankers[j].rank = j + 2;
            if (
              newRankers[j].growing &&
              newRankers[j].you &&
              newRankers[j].multi > 1
            ) {
              newRankers[j].grapes = newRankers[j].grapes.add(1);
            }
            newRankers[j + 1] = newRankers[j];

            // Move current Ranker 1 Place up
            currentRanker.rank = j + 1;
            newRankers[j] = currentRanker;
          } else {
            break;
          }
        }
      }
    }

    console.log(new Date(), deltaSeconds);
    state.rankers.length = 0;
    state.rankers.push(...newRankers);
  }

  return {
    // state
    rankers: state.rankers,
    number,
    types,
    basePointsToPromote,
    // actions
    init,
    changeLadder,
  };
});
