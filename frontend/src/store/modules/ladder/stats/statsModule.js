import Decimal from "break_infinity.js";
import ladderUtils from "@/ladder/utils/ladderUtils";

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
    async calculate({ rootState, commit }) {
      let pointsNeededForManualPromote =
        ladderUtils.calculatePointsNeededForPromote(
          rootState.settings,
          rootState.ladder
        );

      let growingRankerCount = rootState.ladder.rankers.filter(
        (ranker) => ranker.growing
      ).length;

      commit({
        type: "setStats",
        growingRankerCount: growingRankerCount,
        pointsNeededForManualPromote: pointsNeededForManualPromote,
      });
    },
  },
  getters: {},
  modules: {},
};
