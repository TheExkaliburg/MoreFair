import Ladder from "@/ladder/entities/ladder";
import LadderStats from "@/ladder/entities/ladderStats";

export default {
  namespaced: true,
  state: () => {
    return {
      ladder: Ladder.placeholder(),
      stats: new LadderStats(),
    };
  },
  mutations: {
    init(state, { message }) {
      state.ladder = new Ladder(message.content);
    },
    calculate(state, { delta, settings }) {
      state.ladder.calculate(delta, settings);
      state.stats.calculateStats(state.ladder, settings);
    },
    handleNameChange(state, { event }) {
      state.ladder.changeName(event.accountId, event.data);
    },
    handleJoin(state, { event }) {
      state.ladder.addNewRanker(
        event.accountId,
        event.data.username,
        event.data.timesAsshole
      );
    },
    handleAutoPromote(state, { event, settings }) {
      state.ladder.autoPromoteRanker(event.accountId, settings);
    },
    handlePromote(state, { event }) {
      state.ladder.promoteRanker(event.accountId);
    },
    handleSoftResetPoints(state, { event }) {
      state.ladder.softResetRanker(event.accountId);
    },
    handleVinegar(state, { event }) {
      // const vinegarThrown = new Decimal(event.data.amount);
      state.ladder.resetVinegarOfRanker(event.accountId);
      // TODO: Show if you've been graped
    },
    handleMultiplier(state, { event }) {
      state.ladder.multiRanker(event.accountId);
    },
    handleBias(state, { event }) {
      state.ladder.biasRanker(event.accountId);
    },
  }, //
  actions: {
    async update({ rootState, dispatch, commit }, { message, stompClient }) {
      message.events.forEach((event) =>
        dispatch({
          type: "handleEvent",
          event: event,
          stompClient: stompClient,
        })
      );
      commit({
        type: "calculate",
        delta: message.secondsPassed,
        settings: rootState.settings,
      });
    },
    async updateGlobal({ dispatch }, { message, stompClient }) {
      if (message) {
        message.forEach((event) =>
          dispatch({
            type: "handleEvent",
            event: event,
            stompClient: stompClient,
          })
        );
      }
    },
    async handleEvent({ commit, rootState, dispatch }, { event, stompClient }) {
      switch (event.eventType) {
        case "BIAS":
          commit({ type: "handleBias", event: event });
          break;
        case "MULTI":
          commit({ type: "handleMultiplier", event: event });
          break;
        case "VINEGAR":
          commit({ type: "handleVinegar", event: event });
          break;
        case "SOFT_RESET_POINTS":
          commit({ type: "handleSoftResetPoints", event: event });
          break;
        case "PROMOTE":
          commit({
            type: "handlePromote",
            event: event,
          });
          if (event.accountId === rootState.user.accountId) {
            dispatch(
              { type: "incrementHighestLadder", stompClient: stompClient },
              { root: true }
            );
          }
          break;
        case "AUTO_PROMOTE":
          commit({
            type: "handleAutoPromote",
            event: event,
            settings: rootState.settings,
          });
          break;
        case "JOIN":
          commit({ type: "handleJoin", event: event });
          break;
        case "NAME_CHANGE":
          commit({ type: "handleNameChange", event: event });
          break;
        case "CONFIRM":
          // TODO: CONFIRM
          break;
        case "RESET":
          await stompClient.disconnect();
          break;
      }
    },
  },
  getters: {
    shownRankers(state) {
      const numberAtTop = 10;
      const padding = 50;
      const rank = state.ladder.yourRanker.rank;

      state.ladder.rankers.filter(
        (ranker) =>
          ranker.rank <= numberAtTop ||
          (ranker.rank >= rank - padding && ranker.rank <= rank + padding)
      );

      return state.ladder.rankers;
    },
    allRankers(state) {
      return state.ladder.rankers;
    },
  },
};
