import { defineStore } from "pinia";
import { computed, reactive, ref } from "vue";
import Decimal from "break_infinity.js";
import { Ranker, RankerData } from "./entities/ranker";
import { OnTickBody, useStomp } from "~/composables/useStomp";
import { useAPI } from "~/composables/useAPI";
import { useAccountStore } from "~/store/account";

export enum LadderType {
  DEFAULT,
  TINY,
  SMALL,
  BIG,
  GIGANTIC,
  FREE_AUTO,
  NO_AUTO,
  ASSHOLE,
  CHEAP,
  EXPENSIVE,
}

export type LadderData = {
  rankers: RankerData[];
  number: number;
  types: LadderType[];
  basePointsToPromote: string;
};

export type LadderState = {
  rankers: Ranker[];
  number: number;
  types: Set<LadderType>;
  basePointsToPromote: Decimal;
};

export const useLadderStore = defineStore("ladder", () => {
  const api = useAPI();
  const stomp = useStomp();
  const account = useAccountStore();

  const isInitialized = ref<boolean>(false);
  const state = reactive<LadderState>({
    rankers: <Ranker[]>[],
    number: 1,
    types: new Set<LadderType>([LadderType.DEFAULT]),
    basePointsToPromote: new Decimal(0),
  });
  const getters = reactive({
    yourRanker: computed<Ranker | undefined>(() =>
      state.rankers.find((r) => r.accountId === account.state.accountId)
    ),
  });

  function init() {
    if (isInitialized.value) return;
    getLadder(state.number);
  }

  function getLadder(ladderNumber: number) {
    isInitialized.value = true;
    api.ladder
      .getLadder(ladderNumber)
      .then((res) => {
        const data: LadderData = res.data;
        state.rankers = [];
        data.rankers.forEach((ranker) => {
          state.rankers.push(new Ranker(ranker));
        });
        state.types = new Set();
        data.types.forEach((s) => {
          state.types.add(s);
        });
        state.number = data.number;
        state.basePointsToPromote = new Decimal(data.basePointsToPromote);

        if (
          !stomp.callbacks.onLadderEvent.some(
            (x) => x.identifier === "fair_ladder_event"
          )
        ) {
          stomp.callbacks.onLadderEvent.push({
            identifier: "fair_ladder_event",
            callback: (body) => {
              const data: RankerData = body.data;
              const ranker = state.rankers.find(
                (x) => x.accountId === data.accountId
              );
              if (ranker) {
                Object.assign(ranker, new Ranker(data));
              } else {
                state.rankers.push(new Ranker(data));
              }
            },
          });
        }

        if (
          !stomp.callbacks.onTick.some(
            (x) => x.identifier === "fair_ladder_calculateTick"
          )
        ) {
          stomp.callbacks.onTick.push({
            identifier: "fair_ladder_calculateTick",
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
    console.log("changeLadder", newNumber);
    stomp.wsApi.ladder.changeLadder(state.number, newNumber, true);
    getLadder(newNumber);
  }

  function calculateTick(deltaSeconds: number) {
    const delta = new Decimal(deltaSeconds);
    state.rankers.sort((a, b) => b.points.cmp(a.points));

    for (let i = 0; i < state.rankers.length; i++) {
      const ranker = new Ranker(state.rankers[i]);
      state.rankers[i] = ranker;
      state.rankers[i].rank = i + 1;

      // If ranker still on ladder
      if (ranker.growing) {
        // Power & Points
        if (ranker.rank !== 1) {
          ranker.power = ranker.power.add(
            new Decimal((ranker.bias + ranker.rank) * ranker.multi)
              .mul(delta)
              .floor()
          );
        }
        ranker.points = ranker.points.add(ranker.power.mul(delta).floor());

        for (let j = i - 1; j >= 0; j--) {
          const currentRanker = state.rankers[j + 1];
          if (currentRanker.points.cmp(state.rankers[j].points) > 0) {
            // Move 1 position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            state.rankers[j].rank = j + 2;
            if (
              state.rankers[j].growing /* TODO: && rankers[j].you */ &&
              state.rankers[j].multi > 1
            ) {
              state.rankers[j].grapes = state.rankers[j].grapes.add(1);
            }
            state.rankers[j + 1] = state.rankers[j];

            // Move current Ranker 1 Place up
            currentRanker.rank = j + 1;
            state.rankers[j] = currentRanker;
          } else {
            break;
          }
        }
      }
    }
  }

  return {
    state,
    getters,
    // actions
    actions: {
      init,
      changeLadder,
    },
  };
});
