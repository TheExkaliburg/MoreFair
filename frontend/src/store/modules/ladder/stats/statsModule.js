import Decimal from "break_infinity.js";
import ladderUtils from "@/ladder/utils/ladderUtils";
import { Sounds } from "@/modules/sounds";
import { computed } from "vue";
import store from "@/store";

const gotFirstJingleVolume = computed(() =>
  store.getters["options/getOptionValue"]("notificationVolume")
);
const reachingFirstSound = computed(() =>
  store.getters["options/getOptionValue"]("reachingFirstSound")
);

export default {
  namespaced: true,
  state: () => {
    setTimeout(() => {}, 0);
    return {
      growingRankerCount: 1,
      pointsNeededForManualPromote: new Decimal(Infinity),
      playerWasFirstLastTick: true,
    };
  },
  mutations: {
    setStats(
      state,
      {
        growingRankerCount,
        pointsNeededForManualPromote,
        playerWasFirstLastTick,
      }
    ) {
      state.growingRankerCount = growingRankerCount;
      state.pointsNeededForManualPromote = pointsNeededForManualPromote;
      state.playerWasFirstLastTick = playerWasFirstLastTick;
    },
  },
  actions: {
    async calculate({ state, rootState, commit }) {
      let pointsNeededForManualPromote =
        ladderUtils.calculatePointsNeededForPromote(
          rootState.settings,
          rootState.ladder
        );

      let growingRankerCount = rootState.ladder.rankers.filter(
        (ranker) => ranker.growing
      ).length;

      let playerWasFirstLastTick = state.playerWasFirstLastTick;
      //Now we calc the logic for the "hey you just came first" jingle
      if (!playerWasFirstLastTick && rootState.ladder.yourRanker.rank === 1) {
        playerWasFirstLastTick = true;
        if (reachingFirstSound.value) {
          Sounds.play("gotFirstJingle", gotFirstJingleVolume.value);
        }
      }
      if (
        rootState.ladder.yourRanker.you &&
        rootState.ladder.yourRanker.rank !== 1
      ) {
        playerWasFirstLastTick = false;
      }

      commit({
        type: "setStats",
        growingRankerCount: growingRankerCount,
        pointsNeededForManualPromote: pointsNeededForManualPromote,
        playerWasFirstLastTick: playerWasFirstLastTick,
      });
    },
  },
  getters: {},
  modules: {},
};
