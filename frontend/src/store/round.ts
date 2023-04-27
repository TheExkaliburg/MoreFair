import { defineStore } from "pinia";
import { reactive } from "vue";
import { useAPI } from "~/composables/useAPI";
import {
  RoundSettings,
  RoundSettingsData,
} from "~/store/entities/roundSettings";
import {
  OnRoundEventBody,
  RoundEventType,
  useStomp,
} from "~/composables/useStomp";
import { useChatStore } from "~/store/chat";
import { useLadderStore } from "~/store/ladder";
import { useAccountStore } from "~/store/account";
import { useToasts } from "~/composables/useToasts";

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
        state.types = new Set();
        data.types.forEach((t) => state.types.add(t));
        state.assholeLadder = data.assholeLadder;
        state.autoPromoteLadder = data.autoPromoteLadder;
        state.settings = new RoundSettings(data.settings);

        const stomp = useStomp();
        stomp.addCallback(
          stomp.callbacks.onRoundEvent,
          "fair_round_events",
          handleRoundEvent
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
        state.assholeLadder = body.data;
        break;
      case RoundEventType.RESET:
        isInitialized.value = false;
        useStomp().reset();
        setTimeout(() => {
          useRoundStore().actions.reset();
          useChatStore().actions.reset();
          useLadderStore().actions.reset();
          useAccountStore().actions.reset();
          useToasts(
            "Chad was successful in turning back the time, the only thing left from this future is a mark on the initiates that helped in the final ritual."
          );
        }, 6000); // 1 sec more than the reconnect-timer of the stomp client
        break;
      default:
        console.error("Unknown event type", event);
        break;
    }
  }

  return {
    state,
    getters,
    actions: {
      init,
      reset,
    },
  };
});
