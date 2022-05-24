import Decimal from "break_infinity.js";

class LadderStats {
  constructor() {
    this.growingRankerCount = 1;
    this.pointsNeededForManualPromote = new Decimal(Infinity);
  }

  calculateStats(ladder, settings) {
    this.calculatePointsNeededForPromote(ladder, settings);
    this.growingRankerCount = ladder.rankers.filter(
      (ranker) => ranker.growing
    ).length;
  }

  calculatePointsNeededForPromote(ladder, settings) {
    // If not enough Players -> Infinity
    /*
    if (
      ladder.rankers.length <
      Math.max(settings.minimumPeopleForPromote, ladder.ladderNumber)
    ) {
      this.pointsNeededForManualPromote = new Decimal(Infinity);
      return;
    }*/

    // If not enough points -> minimum required Points
    if (
      ladder.firstRanker.points.cmp(
        settings.pointsForPromote.mul(ladder.ladderNumber)
      ) < 0
    ) {
      this.pointsNeededForManualPromote = settings.pointsForPromote.mul(
        ladder.ladderNumber
      );
      return;
    }

    // If before autopromote unlocks -> 1st place
    if (ladder.ladderNumber < settings.autoPromoteLadder) {
      this.pointsNeededForManualPromote = ladder.firstRanker.you
        ? ladder.rankers[1].points.add(1)
        : ladder.firstRanker.points.add(1);
      return;
    }

    let leadingRanker = ladder.firstRanker.you
      ? ladder.yourRanker
      : ladder.firstRanker;
    let pursuingRanker = ladder.firstRanker.you
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
      settings.pointsForPromote.mul(ladder.ladderNumber)
    );
  }
}

export default LadderStats;
