import { defineStore } from "pinia";
import { reactive } from "vue";
import { useAPI } from "~/composables/useAPI";
import {
  RoundSettings,
  RoundSettingsData,
} from "~/store/entities/roundSettings";
import { OnRoundEventBody, useStomp } from "~/composables/useStomp";
import { useToasts } from "~/composables/useToasts";
import { useChatStore } from "~/store/chat";

export enum RoundType {
  DEFAULT = "DEFAULT",
  FAST = "FAST",
  SLOW = "SLOW",
  SHORT = "SHORT",
  LONG = "LONG",
  AUTO = "AUTO",
  CHAOS = "CHAOS",
  RAILROAD = "RAILROAD",
  FARMER = "FARMER",
  RACE = "RACE",
  REVERSE_SCALING = "REVERSE_SCALING",
  SPECIAL_100 = "SPECIAL_100",
}

export enum RoundEventType {
  RESET = "RESET",
  INCREASE_ASSHOLE_LADDER = "INCREASE_ASSHOLE_LADDER",
  INCREASE_TOP_LADDER = "INCREASE_TOP_LADDER",
  JOIN = "JOIN",
}

export type RoundData = {
  settings: RoundSettingsData;
  assholeLadder: number;
  autoPromoteLadder: number;
  topLadder: number;
  types: RoundType[];
};

export const useRoundStore = defineStore("round", () => {
  const api = useAPI();
  const stomp = useStomp();

  const isInitialized = ref<boolean>(false);
  const state = reactive({
    assholeLadder: 20,
    autoPromoteLadder: 1,
    types: new Set([RoundType.DEFAULT]),
    topLadder: 1,
    settings: new RoundSettings({}),
  });
  const getters = reactive({
    formattedTypes: computed(() => {
      return Array.from(state.types).join(",");
    }),
  });

  function init() {
    if (isInitialized.value) return;
    getCurrentRound();
  }

  function reset() {
    isInitialized.value = false;
    init();
  }

  function getCurrentRound() {
    isInitialized.value = true;
    api.round
      .getCurrentRound()
      .then((res) => {
        const data: RoundData = res.data;
        state.types.clear();
        data.types.forEach((t) => state.types.add(t));
        state.assholeLadder = data.assholeLadder;
        state.autoPromoteLadder = data.autoPromoteLadder;
        state.settings = new RoundSettings(data.settings);
        state.topLadder = data.topLadder;

        stomp.addCallback(
          stomp.callbacks.onRoundEvent,
          "fair_round_events",
          handleRoundEvent,
        );
      })
      .catch((_) => {
        isInitialized.value = false;
      });
  }

  function handleRoundEvent(body: OnRoundEventBody) {
    const event = body.eventType;
    switch (event) {
      case RoundEventType.INCREASE_ASSHOLE_LADDER:
        state.assholeLadder = Math.max(body.data, state.assholeLadder);
        break;
      case RoundEventType.INCREASE_TOP_LADDER:
        state.topLadder = Math.max(body.data, state.topLadder);
        break;
      case RoundEventType.RESET:
        isInitialized.value = false;
        useToasts(
          "Chad was successful in turning back the time, the only thing left from this future is a mark on the initiates that helped in the final ritual.",
          { autoClose: false },
        );
        useStomp().reset();
        break;
      case RoundEventType.JOIN:
        useChatStore().state.suggestions.push(body.data);
        break;
      default:
        console.error("Unknown event type", event);
        break;
    }
  }

  // setTimeout(() => handleRoundEvent({ eventType: RoundEventType.RESET }), 1000);

  return {
    state,
    getters,
    actions: {
      init,
      reset,
    },
  };
});
