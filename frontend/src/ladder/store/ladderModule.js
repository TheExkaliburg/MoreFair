import Ladder from "@/ladder/entities/ladder";

export default {
  namespaced: true,
  state: () => {
    return {
      ladder: {},
    };
  },
  mutations: {
    init(state, payload) {
      state.ladder = new Ladder(payload.message.content);
    },
    calculate(state, { delta, settings }) {
      state.ladder.calculate(delta, settings);
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
    update({ message, rootState, dispatch, commit }) {
      if (message) {
        message.events.forEach((event) =>
          dispatch({ type: "handleEvent", event: event })
        );
        commit({
          type: "calculate",
          delta: message.secondsPassed,
          settings: rootState.settings,
        });
      }
    },
    updateGlobal({ message, dispatch }) {
      if (message) {
        message.forEach((event) =>
          dispatch({ type: "handleEvent", event: event })
        );
      }
    },
    handleEvent({ event, commit, rootState }) {
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
            // TODO: Go up a ladder
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
          // TODO: RESET
          break;
      }
    },
  },
  getters: {},
};
