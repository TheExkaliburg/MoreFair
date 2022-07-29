import Ranker from "@/ladder/entities/ranker";
import Decimal from "break_infinity.js";
import ladderUtils from "@/ladder/utils/ladderUtils";

export default {
  setLadder(state, { number, rankers, ladderTypes, basePointsToPromote }) {
    state.number = number;
    state.types = new Set(ladderTypes);
    state.basePointsToPromote = new Decimal(basePointsToPromote);
    state.rankers = [];
    rankers.forEach((ranker) => {
      const r = new Ranker(ranker);
      state.rankers.push(r);
    });
    state.yourRanker = state.rankers.find((r) => r.you);
    if (!state.yourRanker) {
      state.yourRanker = new Ranker();
    }
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
  handleThrowVinegarEvent(state, { event, numberFormatter }) {
    const vinegarThrown = new Decimal(event.data.amount);
    let throwingRanker;
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId) {
        if (ranker.you) ranker.vinegar = new Decimal(0);
        throwingRanker = ranker;
      }
    });

    const yourRanker = state.yourRanker;
    if (yourRanker.accountId === event.data.targetId) {
      //We are in the graped position
      yourRanker.vinegar = yourRanker.vinegar.sub(vinegarThrown);
      if (event.data.success && throwingRanker) {
        yourRanker.vinegar = new Decimal(0);
        setTimeout(
          () =>
            alert(
              "You slipped down the ladder because " +
                throwingRanker.username +
                " (#" +
                throwingRanker.accountId +
                ") threw their " +
                numberFormatter.format(vinegarThrown) +
                " Vinegar at you!"
            ),
          0
        );
      }
    }
  },
  handleSoftResetPointsEvent(state, { event }) {
    state.rankers.forEach((ranker) => {
      if (event.accountId === ranker.accountId) {
        ranker.points = new Decimal(0);
        ranker.power = ranker.power.div(new Decimal(2)).floor();
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
      ahPoints: event.data.ahPoints,
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
