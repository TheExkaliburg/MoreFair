import Decimal from "break_infinity.js";
import Ranker from "@/ladder/entities/ranker";

export default {
  async setup({ commit, rootState }, { message }) {
    await commit({
      type: "setLadder",
      number: message.content.number,
      rankers: message.content.rankers,
      settings: rootState.settings,
    });
  },
  async handleLadderEvent({ dispatch }, { message, stompClient }) {
    console.log(message);
    if (message) {
      dispatch({
        type: "handleEvent",
        event: message,
        stompClient: stompClient,
      });
    }
  },
  async handleGlobalEvent({ dispatch }, { message, stompClient }) {
    console.log(message);
    if (message) {
      dispatch({
        type: "handleEvent",
        event: message,
        stompClient: stompClient,
      });
    }
  },
  async handlePrivateEvent({ dispatch }, { message, stompClient }) {
    console.log(message);
    if (message) {
      dispatch({
        type: "handleEvent",
        event: message,
        stompClient: stompClient,
      });
    }
  },
  async calculate({ state, rootState, commit, getters }, { message }) {
    const delta = new Decimal(message.delta);
    let rankers = [...state.rankers];
    rankers.sort((a, b) => b.points.sub(a.points));
    let yourRanker = new Ranker(state.yourRanker);
    for (let i = 0; i < rankers.length; i++) {
      let ranker = new Ranker(rankers[i]);
      rankers[i] = ranker;
      rankers[i].rank = i + 1;
      if (yourRanker.accountId === ranker.accountId) yourRanker = ranker;

      // If the ranker is currently still on ladder
      if (ranker.growing) {
        // ladderStats.growingRankerCount += 1;
        // Calculating Points & Power
        if (ranker.rank !== 1)
          ranker.power = ranker.power.add(
            new Decimal((ranker.bias + ranker.rank - 1) * ranker.multi)
              .mul(delta)
              .floor()
          );
        ranker.points = ranker.points.add(ranker.power.mul(delta).floor());

        // Calculating Vinegar based on Grapes count
        if (ranker.rank !== 1 && ranker.you)
          ranker.vinegar = ranker.vinegar.add(ranker.grapes.mul(delta).floor());

        if (ranker.rank === 1 && ranker.you && getters.isLadderUnlocked)
          ranker.vinegar = ranker.vinegar
            .mul(Decimal.pow(new Decimal(0.9975), delta))
            .floor();

        rankers[i] = ranker;

        for (let j = i - 1; j >= 0; j--) {
          // If one of the already calculated Rankers have less points than this ranker
          // swap these in the list... This way we keep the list sorted, theoretically
          let currentRanker = rankers[j + 1];
          if (currentRanker.points.cmp(rankers[j].points) > 0) {
            // Move 1 Position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            rankers[j].rank = j + 2;
            if (rankers[j].growing && rankers[j].you && rankers[j].multi > 1)
              rankers[j].grapes = rankers[j].grapes.add(new Decimal(1));
            rankers[j + 1] = rankers[j];

            // Move this Ranker 1 Place up
            currentRanker.rank = j + 1;
            rankers[j] = currentRanker;
          } else {
            break;
          }
        }
      }
    }
    // Ranker on Last Place gains 1 Grape, only if he isn't the only one
    if (rankers.length >= 1) {
      let index = rankers.length - 1;
      if (rankers[index].growing && rankers[index].you)
        rankers[index].grapes = rankers[index].grapes.add(
          new Decimal(3).mul(delta).floor()
        );
    }
    commit({
      type: "setLadder",
      rankers: rankers,
      number: state.number,
      settings: rootState.settings,
    });
  },
  async handleEvent({ commit, rootState, dispatch }, { event, stompClient }) {
    switch (event.eventType) {
      case "BUY_BIAS":
        commit({ type: "handleBiasEvent", event: event });
        break;
      case "BUY_MULTI":
        commit({ type: "handleMultiEvent", event: event });
        break;
      case "BUY_AUTO_PROMOTE":
        commit({
          type: "handleAutoPromoteEvent",
          event: event,
          settings: rootState.settings,
        });
        break;
      case "THROW_VINEGAR":
        commit({ type: "handleThrowVinegarEvent", event: event });
        break;
      case "SOFT_RESET_POINTS":
        commit({ type: "handleSoftResetPointsEvent", event: event });
        break;
      case "PROMOTE":
        commit({
          type: "handlePromoteEvent",
          event: event,
        });
        if (event.accountId === rootState.user.accountId) {
          dispatch(
            { type: "incrementHighestLadder", stompClient: stompClient },
            { root: true }
          );
        }
        break;
      case "JOIN":
        commit({ type: "handleJoinEvent", event: event });
        break;
      case "NAME_CHANGE":
        commit({ type: "handleNameChangeEvent", event: event });
        break;
      case "CONFIRM":
        // TODO: CONFIRM
        break;
      case "RESET":
        await stompClient.reconnect();
        break;
    }
  },
};
