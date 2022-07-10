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
    setTimeout(() => {
      Sounds.register("gotFirstJingle", require("@/assets/gotFirstJingle.wav"));
    }, 0);
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
      let pointsNeededForManualPromote = calculatePointsNeededForPromote(
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

function calculatePointsNeededForPromote(settings, ladder) {
  // If not enough Players -> Infinity
  if (ladder.rankers.length <= 1) {
    return new Decimal(Infinity);
  }

  // If not enough points -> minimum required Points
  if (
    ladder.rankers[0].points.cmp(
      ladderUtils.getMinimumPointsForPromote(settings, ladder)
    ) < 0
  ) {
    return settings.pointsForPromote.mul(ladder.number);
  }

  // If before autopromote unlocks -> 1st place
  if (ladder.number < settings.autoPromoteLadder) {
    return ladder.rankers[0].you
      ? ladder.rankers[1].points.add(1)
      : ladder.rankers[0].points.add(1);
  }

  let leadingRanker = ladder.rankers[0].you
    ? ladder.yourRanker
    : ladder.rankers[0];
  let pursuingRanker = ladder.rankers[0].you
    ? ladder.rankers[1]
    : ladder.yourRanker;

  // How many more points does the ranker gain against his pursuer, every Second
  let powerDiff = (
    leadingRanker.growing ? leadingRanker.power : new Decimal(0)
  ).sub(pursuingRanker.growing ? pursuingRanker.power : 0);
  // Calculate the needed Point difference, to have f.e. 30seconds of point generation with the difference in power
  let neededPointDiff = powerDiff.mul(settings.manualPromoteWaitTime).abs();

  return Decimal.max(
    (leadingRanker.you ? pursuingRanker : leadingRanker).points.add(
      neededPointDiff
    ),
    ladderUtils.getMinimumPointsForPromote(settings, ladder)
  );
}
