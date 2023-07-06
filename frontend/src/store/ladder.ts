import { defineStore } from "pinia";
import { computed, reactive, ref } from "vue";
import Decimal from "break_infinity.js";
import { Ranker, RankerData } from "./entities/ranker";
import {
  OnLadderEventBody,
  OnTickBody,
  useStomp,
} from "~/composables/useStomp";
import { useAPI } from "~/composables/useAPI";
import { useAccountStore } from "~/store/account";
import { useLadderUtils } from "~/composables/useLadderUtils";
import { useChatStore } from "~/store/chat";
import { useFormatter } from "~/composables/useFormatter";
import { useToasts } from "~/composables/useToasts";
import { SOUNDS, useSound } from "~/composables/useSound";
import { useOptionsStore } from "~/store/options";

export enum LadderType {
  DEFAULT = "DEFAULT",
  TINY = "TINY",
  SMALL = "SMALL",
  BIG = "BIG",
  GIGANTIC = "GIGANTIC",
  FREE_AUTO = "FREE_AUTO",
  NO_AUTO = "NO_AUTO",
  ASSHOLE = "ASSHOLE",
  CHEAP = "CHEAP",
  EXPENSIVE = "EXPENSIVE",
  BOUNTIFUL = "BOUNTIFUL",
  DROUGHT = "DROUGHT",
  CONSOLATION = "CONSOLATION",
  NO_HANDOUTS = "NO_HANDOUTS",
  GENEROUS = "GENEROUS",
  STINGY = "STINGY",
}

export enum LadderEventType {
  BUY_BIAS = "BUY_BIAS",
  BUY_MULTI = "BUY_MULTI",
  REMOVE_MULTI = "REMOVE_MULTI",
  BUY_AUTO_PROMOTE = "BUY_AUTO_PROMOTE",
  THROW_VINEGAR = "THROW_VINEGAR",
  SOFT_RESET_POINTS = "SOFT_RESET_POINTS",
  PROMOTE = "PROMOTE",
  JOIN = "JOIN",
  ADD_FREE_AUTO = "ADD_FREE_AUTO",
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
  const chatStore = useChatStore();
  const optionsStore = useOptionsStore();
  const accountStore = useAccountStore();
  const ladderUtils = useLadderUtils();

  const isInitialized = ref<boolean>(false);
  const state = reactive<LadderState>({
    rankers: <Ranker[]>[],
    number: 1,
    types: new Set<LadderType>([LadderType.DEFAULT]),
    basePointsToPromote: new Decimal(0),
  });
  const ladderEvents: OnLadderEventBody[] = [];

  const getters = reactive({
    yourRanker: computed<Ranker | undefined>(() =>
      state.rankers.find((r) => r.accountId === accountStore.state.accountId)
    ),
    activeRankers: computed<number>(
      () => state.rankers.filter((r) => r.growing).length
    ),
    formattedTypes: computed(() => {
      return Array.from(state.types).join(",");
    }),
    shownRankers: computed<Ranker[]>(() => {
      let result = state.rankers;
      if (optionsStore.state.ladder.hidePromotedPlayers.value) {
        result = result.filter(
          (r) => r.growing || r.accountId === getters.yourRanker?.accountId
        );
      }
      if (!optionsStore.state.ladder.showAllRankers.value) {
        const top = optionsStore.state.ladder.showTopRankers.value;
        const above = optionsStore.state.ladder.showAboveRankers.value;
        const below = optionsStore.state.ladder.showBelowRankers.value;
        const bottom = optionsStore.state.ladder.showBottomRankers.value;

        result = result.filter((r) => {
          if (r.rank <= top) return true;
          if (r.rank > state.rankers.length - bottom) return true;
          if (getters.yourRanker === undefined) return false;
          if (getters.yourRanker.accountId === r.accountId) return true;
          return (
            r.rank >= getters.yourRanker?.rank - above &&
            r.rank <= getters.yourRanker.rank + below
          );
        });
      }

      return result;
    }),
  });

  function init() {
    if (isInitialized.value) return;
    getLadder(accountStore.state.highestCurrentLadder);
  }

  function reset() {
    isInitialized.value = false;
    init();
  }

  function getLadder(ladderNumber: number) {
    isInitialized.value = true;
    api.ladder
      .getLadder(ladderNumber)
      .then((res) => {
        const data: LadderData = res.data;
        state.rankers.length = 0;
        data.rankers.forEach((ranker) => {
          state.rankers.push(new Ranker(ranker));
        });
        state.types.clear();
        data.types.forEach((s) => {
          state.types.add(s);
        });
        state.number = data.number;
        state.basePointsToPromote = new Decimal(data.basePointsToPromote);

        stomp.addCallback(
          stomp.callbacks.onLadderEvent,
          "fair_ladder_events",
          (body) => {
            ladderEvents.push(body);
          }
        );

        stomp.addCallback(
          stomp.callbacks.onTick,
          "fair_ladder_calculateTick",
          (body: OnTickBody) => calculateTick(body.delta)
        );
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function changeLadder(newNumber: number) {
    stomp.wsApi.ladder.changeLadder(newNumber);
    getLadder(newNumber);
  }

  function calculateTick(deltaSeconds: number) {
    const wasFirst = getters.yourRanker?.rank === 1;

    handleEvents();
    ladderEvents.length = 0;

    const delta = new Decimal(deltaSeconds);
    state.rankers.sort((a, b) => b.points.cmp(a.points));

    for (let i = 0; i < state.rankers.length; i++) {
      state.rankers[i].rank = i + 1;

      // If ranker still on ladder
      if (state.rankers[i].growing) {
        // Power & Points
        if (state.rankers[i].rank !== 1) {
          state.rankers[i].power = Object.freeze(
            state.rankers[i].power
              .add(state.rankers[i].getPowerPerSecond().mul(delta))
              .floor()
          );
        }
        state.rankers[i].points = Object.freeze(
          state.rankers[i].points.add(state.rankers[i].power.mul(delta).floor())
        );

        for (let j = i - 1; j >= 0; j--) {
          const currentRanker = state.rankers[j + 1];
          if (currentRanker.points.cmp(state.rankers[j].points) > 0) {
            // Move 1 position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            state.rankers[j].rank = j + 2;
            if (
              state.rankers[j].growing &&
              state.rankers[j].accountId === getters.yourRanker?.accountId &&
              state.rankers[j].multi > 1
            ) {
              state.rankers[j].grapes = Object.freeze(
                state.rankers[j].grapes.add(ladderUtils.getPassingGrapes())
              );
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

    const yourRanker = getters.yourRanker;
    if (yourRanker !== undefined && yourRanker.growing) {
      if (yourRanker.rank !== 1) {
        yourRanker.vinegar = Object.freeze(
          yourRanker.vinegar.add(yourRanker.grapes.mul(deltaSeconds).floor())
        );
      }
      if (yourRanker.rank === 1 && ladderUtils.isLadderPromotable.value) {
        yourRanker.vinegar = Object.freeze(
          yourRanker.vinegar
            .mul(Decimal.pow(new Decimal(0.9975), deltaSeconds))
            .floor()
        );
      }

      if (
        yourRanker.rank === state.rankers.length &&
        state.rankers.length >= 1
      ) {
        yourRanker.grapes = Object.freeze(
          yourRanker.grapes.add(new Decimal(ladderUtils.getBottomGrapes()))
        );
      }
    }

    const isFirst = getters.yourRanker?.rank === 1;
    if (isFirst && !wasFirst) {
      useSound(SOUNDS.GOT_FIRST).play();
    }
  }

  function handleEvents() {
    const eventsLength = ladderEvents.length;
    for (let i = 0; i < eventsLength; i++) {
      const event = ladderEvents[i];
      let ranker = state.rankers.find((r) => r.accountId === event.accountId);
      if (ranker === undefined && event.eventType === LadderEventType.JOIN)
        ranker = new Ranker({});
      if (ranker === undefined) break;

      switch (event.eventType) {
        case LadderEventType.BUY_BIAS:
          ranker.bias += 1;
          ranker.points = Object.freeze(new Decimal(0));
          break;
        case LadderEventType.BUY_MULTI:
          ranker.multi += 1;
          ranker.bias = 0;
          ranker.points = Object.freeze(new Decimal(0));
          ranker.power = Object.freeze(new Decimal(0));
          break;
        case LadderEventType.REMOVE_MULTI:
          ranker.multi = Math.max(1, ranker.multi - 1);
          ranker.bias = 0;
          ranker.points = Object.freeze(new Decimal(0));
          ranker.power = Object.freeze(new Decimal(0));
          break;
        case LadderEventType.BUY_AUTO_PROMOTE:
          ranker.autoPromote = true;
          ranker.grapes = Object.freeze(
            ranker.grapes.sub(
              new Decimal(ladderUtils.getAutoPromoteCost(ranker.rank))
            )
          );
          break;
        case LadderEventType.THROW_VINEGAR:
          handleThrowVinegarEvent(ranker, event);
          break;
        case LadderEventType.SOFT_RESET_POINTS:
          ranker.points = Object.freeze(new Decimal(0));
          ranker.power = Object.freeze(
            ranker.power.div(new Decimal(2)).floor()
          );
          break;
        case LadderEventType.PROMOTE:
          ranker.growing = false;
          if (ranker.accountId === getters.yourRanker?.accountId) {
            useChatStore().actions.changeChat(state.number + 1);
            useLadderStore().actions.changeLadder(state.number + 1);
            useAccountStore().state.highestCurrentLadder = state.number + 1;
          }
          break;
        case LadderEventType.JOIN:
          handleJoinEvent(event);
          break;
        case LadderEventType.ADD_FREE_AUTO:
          state.types.add(LadderType.FREE_AUTO);
          useToasts(
            `Since other rankers breached another Ladder, everyone on this ladder got gifted a free auto promote! (No Refunds)`,
            { autoClose: 60 }
          );
          break;
        default:
          console.error("Unknown event type", event);
          break;
      }
    }
  }

  function handleThrowVinegarEvent(ranker: Ranker, event: OnLadderEventBody) {
    if (getters.yourRanker === undefined) return;
    const vinegarThrown = new Decimal(event.data.amount);
    if (ranker.accountId === getters.yourRanker.accountId) {
      ranker.vinegar = Object.freeze(new Decimal(0));
      return;
    }

    if (event.data.targetId === getters.yourRanker.accountId) {
      getters.yourRanker.vinegar = Object.freeze(
        Decimal.max(
          getters.yourRanker.vinegar.sub(vinegarThrown),
          new Decimal(0)
        )
      );
      useToasts(
        `${ranker.username} (#${ranker.accountId}) ${
          event.data.success ? "successfully" : ""
        } threw  ${useFormatter(vinegarThrown)} vinegar at you!`
      );
      chatStore.actions.addSystemMessage(
        `{@} saw {@} throwing vinegar at {@}. They've ${
          event.data.success ? "successfully" : ""
        } used ${useFormatter(vinegarThrown)} vinegar!`,
        JSON.stringify([
          { u: "Chad", i: 0, id: 1 },
          { u: ranker.username, i: 8, id: ranker.accountId },
          {
            u: getters.yourRanker.username,
            i: 32,
            id: getters.yourRanker.accountId,
          },
        ])
      );
    }
  }

  function handleJoinEvent(event: OnLadderEventBody) {
    if (state.rankers.some((r) => r.accountId === event.accountId)) return;
    const data = event.data;
    data.accountId = event.accountId;
    state.rankers.push(new Ranker(data));
  }

  return {
    state,
    getters,
    // actions
    actions: {
      init,
      reset,
      changeLadder,
    },
  };
});
