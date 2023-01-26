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
        rankers.length = 0;
        data.rankers.forEach((ranker) => {
          rankers.push(new Ranker(ranker));
          rankers.push(new Ranker(ranker));
          rankers.push(new Ranker(ranker));
          rankers.push(new Ranker(ranker));
          rankers.push(new Ranker(ranker));
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
            callback: (body) => {
              const data: RankerData = body.data;
              const ranker = rankers.find(
                (x) => x.accountId === data.accountId
              );
              if (ranker) {
                Object.assign(ranker, new Ranker(data));
              } else {
                rankers.push(new Ranker(data));
              }
            },
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
    // const newRankers: Ranker[] = [...rankers];
    rankers.sort((a, b) => b.points.cmp(a.points));

    for (let i = 0; i < rankers.length; i++) {
      const ranker = new Ranker(rankers[i]);
      rankers[i] = ranker;
      rankers[i].rank = i + 1;

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
        // rankers[i] = ranker;

        for (let j = i - 1; j >= 0; j--) {
          const currentRanker = rankers[j + 1];
          if (currentRanker.points.cmp(rankers[j].points) > 0) {
            // Move 1 position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            rankers[j].rank = j + 2;
            if (
              rankers[j].growing /* TODO: && rankers[j].you */ &&
              rankers[j].multi > 1
            ) {
              rankers[j].grapes = rankers[j].grapes.add(1);
            }
            rankers[j + 1] = rankers[j];

            // Move current Ranker 1 Place up
            currentRanker.rank = j + 1;
            rankers[j] = currentRanker;
          } else {
            break;
          }
        }
      }
    }
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
