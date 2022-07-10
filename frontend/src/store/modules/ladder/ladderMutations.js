import Ranker from "@/ladder/entities/ranker";
import Decimal from "break_infinity.js";
import ladderUtils from "@/ladder/utils/ladderUtils";

export default {
  setLadder(state, { number, rankers, ladderType, basePointsToPromote }) {
    state.number = number;
    state.type = ladderType;
    state.basePointsToPromote = new Decimal(basePointsToPromote);
    state.rankers = [];
    rankers.forEach((ranker) => {
      const r = new Ranker(ranker);
      state.rankers.push(r);
    });
    state.yourRanker = state.rankers.find((r) => r.you);
  },
  handleMultiEvent(state, { event }) {
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId && ranker.growing) {
        ranker.multi += 1;
        ranker.bias = 0;
        ranker.points = new Decimal(0);
        ranker.power = new Decimal(0);
      }
    });
  },
  handleBiasEvent(state, { event }) {
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId && ranker.growing) {
        ranker.bias += 1;
        ranker.points = new Decimal(0);
      }
    });
  },
  handleAutoPromoteEvent(state, { event, settings }) {
    state.rankers.forEach((ranker) => {
      if (ranker.you && event.accountId === ranker.accountId) {
        ranker.grapes = ranker.grapes.sub(
          ladderUtils.getAutoPromoteCost(settings, state, ranker.rank)
        );
        ranker.autoPromote = true;
      }
    });
  },
  handleThrowVinegarEvent(state, { event }) {
    // const vinegarThrown = new Decimal(event.data.amount);
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId) {
        if (ranker.you) ranker.vinegar = new Decimal(0);
      }
    });
    // TODO: Show if you've been graped
    const yourRanker = state.ladder.yourRanker;
    if (yourRanker.rank === 1) {
      //We are in the graped position
      this.rankers.forEach((ranker) => {
        if (event.accountId === ranker.accountId) {
          if (ranker.you)
            ranker.vinegar = ranker.vinegar.sub(event.data.amount);
        }
      });
    }
  },
  handleSoftResetPointsEvent(state, { event }) {
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId) {
        ranker.points = new Decimal(0);
      }
    });
  },
  handleNameChangeEvent(state, { event }) {
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId) {
        ranker.username = event.data;
      }
    });
  },
  handleJoinEvent(state, { event }) {
    let newRanker = new Ranker({
      accountId: event.accountId,
      username: event.data.username,
      you: false,
      tag: event.data.tag,
    });
    if (
      !state.rankers.find(
        (r) => r.accountId === newRanker.accountId && r.growing
      )
    ) {
      state.rankers.push(newRanker);
    }
  },
  handlePromoteEvent(state, { event }) {
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId) {
        ranker.growing = false;
      }
    });
  },
};
