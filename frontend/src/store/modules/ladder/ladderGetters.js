import ladderUtils from "@/ladder/utils/ladderUtils";

export default {
  shownRankers(state, getters, rootState, rootGetters) {
    const showAllRankers =
      rootGetters["options/getOptionValue"]("showAllRankers");

    if (showAllRankers) return getters.allRankers;

    const numberAtTop = rootGetters["options/getOptionValue"]("rankersAtTop");
    const padding = rootGetters["options/getOptionValue"]("rankersPadding");
    const rank = state.yourRanker.rank;

    return state.rankers.filter(
      (ranker) =>
        ranker.rank <= numberAtTop ||
        (ranker.rank >= rank - padding && ranker.rank <= rank + padding)
    );
  },
  allRankers(state) {
    return state.rankers;
  },
  activeRankers(state) {
    return state.rankers.filter((ranker) => ranker.growing);
  },
  getMinimumPointsForPromote(state, _, rootState) {
    return ladderUtils.getMinimumPointsForPromote(rootState.settings, state);
  },
  getMinimumPeopleForPromote(state, _, rootState) {
    return ladderUtils.getMinimumPeopleForPromote(rootState.settings, state);
  },
  getAutoPromoteCost(state, _, rootState) {
    return ladderUtils.getAutoPromoteCost(
      rootState.settings,
      state,
      state.yourRanker.rank
    );
  },
  isLadderUnlocked(state, _, rootState) {
    return ladderUtils.isLadderUnlocked(rootState.settings, state);
  },
  canThrowVinegar(state, _, rootState) {
    return ladderUtils.canThrowVinegar(rootState.settings, state);
  },
  getNextUpgradeCost: (state) => (currentUpgrade) => {
    return ladderUtils.getNextUpgradeCost(state, currentUpgrade);
  },
  getVinegarThrowCost(state, _, rootState) {
    return ladderUtils.getVinegarThrowCost(rootState.settings, state);
  },
  canPromote(state, _, rootState) {
    return ladderUtils.canPromote(rootState.settings, state);
  },
};
