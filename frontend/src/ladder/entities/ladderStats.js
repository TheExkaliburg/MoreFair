import { Sounds } from "@/modules/sounds";
import store from "@/store";
import Decimal from "break_infinity.js";
import { computed } from "vue";

const gotFirstJingleVolume = computed(() =>
  store.getters["options/getOptionValue"]("notificationVolume")
);
const reachingFirstSound = computed(() =>
  store.getters["options/getOptionValue"]("reachingFirstSound")
);

class LadderStats {
  constructor({
    growingRankerCount = 1,
    pointsNeededForManualPromote = new Decimal(Infinity),
    playerWasFirstLastTick = true, //assume we were first so we dont jingle every time we change the ladder.
  } = {}) {
    this.growingRankerCount = growingRankerCount;
    this.pointsNeededForManualPromote = pointsNeededForManualPromote;
    this.playerWasFirstLastTick = playerWasFirstLastTick;

    //Since we get instanciated in a modules sub module, we need to do a setTimeout to make sure the modules is ready.
    //This ensures that the modules is fully created and in the next tick we can access the modules normally.
    setTimeout(() => {
      Sounds.register("gotFirstJingle", require("@/assets/gotFirstJingle.wav"));
    }, 0);
  }

  calculateStats(ladder, settings) {
    this.calculatePointsNeededForPromote(ladder, settings);
    this.growingRankerCount = ladder.rankers.filter(
      (ranker) => ranker.growing
    ).length;

    //Now we calc the logic for the "hey you just came first" jingle
    if (!this.playerWasFirstLastTick && ladder.yourRanker.rank === 1) {
      this.playerWasFirstLastTick = true;
      if (reachingFirstSound.value) {
        Sounds.play("gotFirstJingle", gotFirstJingleVolume.value);
      }
    }
    if (ladder.yourRanker.you && ladder.yourRanker.rank !== 1) {
      this.playerWasFirstLastTick = false;
    }
  }

  calculatePointsNeededForPromote(ladder, settings) {
    // If not enough Players -> Infinity
    /*
    if (
      ladder.rankers.length <
      Math.max(settings.minimumPeopleForPromote, ladder.number)
    ) {
      this.pointsNeededForManualPromote = new Decimal(Infinity);
      return;
    }*/

    // If not enough points -> minimum required Points
    if (
      ladder.rankers[0].points.cmp(
        ladder.getMinimumPointsForPromote(settings)
      ) < 0
    ) {
      this.pointsNeededForManualPromote = settings.pointsForPromote.mul(
        ladder.number
      );
      return;
    }

    // If before autopromote unlocks -> 1st place
    if (ladder.number < settings.autoPromoteLadder) {
      this.pointsNeededForManualPromote = ladder.firstRanker.you
        ? ladder.rankers[1].points.add(1)
        : ladder.rankers[0].points.add(1);
      return;
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

    this.pointsNeededForManualPromote = Decimal.max(
      (leadingRanker.you ? pursuingRanker : leadingRanker).points.add(
        neededPointDiff
      ),
      ladder.getMinimumPointsForPromote(settings)
    );
  }
}

export default LadderStats;
