import Ranker from "@/ladder/entities/ranker";
import Decimal from "break_infinity.js";

class Ladder {
  constructor(data) {
    this.currentLadderNum = data.currentLadder.number;
    this.rankers = [];
    data.rankers.forEach((ranker) => {
      const r = new Ranker(ranker);
      this.rankers.push(r);
    });
    this.startRank = data.startRank;
    this.yourRanker = new Ranker(data.yourRanker);
    this.firstRanker = new Ranker(data.firstRanker);
  }

  calculate(delta, settings) {
    this.rankers = this.rankers.sort((a, b) =>
      new Decimal(b.points).sub(a.points)
    );
    // ladderStats.growingRankerCount = 0;
    for (let i = 0; i < this.rankers.length; i++) {
      this.rankers[i].rank = i + 1;
      // If the ranker is currently still on ladder
      if (this.rankers[i].growing) {
        // ladderStats.growingRankerCount += 1;
        // Calculating Points & Power
        if (this.rankers[i].rank !== 1)
          this.rankers[i].power = this.rankers[i].power.add(
            new Decimal(
              (this.rankers[i].bias + this.rankers[i].rank - 1) *
                this.rankers[i].multiplier
            )
              .mul(new Decimal(delta))
              .floor()
          );
        this.rankers[i].points = this.rankers[i].points.add(
          this.rankers[i].power.mul(delta).floor()
        );

        // Calculating Vinegar based on Grapes count
        if (this.rankers[i].rank !== 1 && this.rankers[i].you)
          this.rankers[i].vinegar = this.rankers[i].vinegar.add(
            this.rankers[i].grapes.mul(delta).floor()
          );

        if (
          this.rankers[i].rank === 1 &&
          this.rankers[i].you &&
          this.isLadderUnlocked(settings)
        )
          this.rankers[i].vinegar = this.rankers[i].vinegar
            .mul(Math.pow(0.9975, delta))
            .floor();

        for (let j = i - 1; j >= 0; j--) {
          // If one of the already calculated Rankers have less points than this ranker
          // swap these in the list... This way we keep the list sorted, theoretically
          let currentRanker = this.rankers[j + 1];
          if (currentRanker.points.cmp(this.rankers[j].points) > 0) {
            // Move 1 Position up and move the ranker there 1 Position down

            // Move other Ranker 1 Place down
            this.rankers[j].rank = j + 2;
            if (
              this.rankers[j].growing &&
              this.rankers[j].you &&
              this.rankers[j].multiplier > 1
            )
              this.rankers[j].grapes = this.rankers[j].grapes.add(
                new Decimal(1)
              );
            this.rankers[j + 1] = this.rankers[j];

            // Move this Ranker 1 Place up
            currentRanker.rank = j + 1;
            this.rankers[j] = currentRanker;
          } else {
            break;
          }
        }
      }
    }
  }

  multiRanker(accountId) {
    this.rankers.forEach((ranker) => {
      if (accountId === ranker.accountId && ranker.growing) {
        ranker.multiplier += 1;
        ranker.bias = 0;
        ranker.points = new Decimal(0);
        ranker.power = new Decimal(0);
      }
    });
  }

  biasRanker(accountId) {
    this.rankers.forEach((ranker) => {
      if (accountId === ranker.accountId && ranker.growing) {
        ranker.bias += 1;
        ranker.points = new Decimal(0);
      }
    });
  }

  softResetRanker(accountId) {
    this.rankers.forEach((ranker) => {
      if (accountId === ranker.accountId) {
        ranker.points = new Decimal(0);
      }
    });
  }

  changeName(accountId, newName) {
    this.rankers.forEach((ranker) => {
      if (accountId === ranker.accountId) {
        ranker.username = newName;
      }
    });
  }

  addNewRanker(accountId, username, timesAsshole) {
    let newRanker = new Ranker({
      accountId: accountId,
      username: username,
      points: new Decimal(0),
      power: new Decimal(1),
      bias: 0,
      multiplier: 1,
      you: false,
      growing: true,
      timesAsshole: timesAsshole,
    });

    if (!this.rankers.find((r) => r.accountId === accountId && r.growing)) {
      this.rankers.push(newRanker);
    }
  }

  autoPromoteRanker(accountId, settings) {
    this.rankers.forEach((ranker) => {
      if (ranker.you && accountId === ranker.accountId) {
        ranker.grapes = ranker.grapes.sub(
          this.getAutoPromoteCost(this.currentLadderNum, ranker.rank, settings)
        );
        ranker.autoPromote = true;
      }
    });
  }

  promoteRanker(accountId) {
    this.rankers.forEach((ranker) => {
      if (accountId === ranker.accountId) {
        ranker.growing = false;
      }
    });
  }

  resetVinegarOfRanker(accountId) {
    this.rankers.forEach((ranker) => {
      if (accountId === ranker.accountId) {
        if (ranker.you) ranker.vinegar = new Decimal(0);
      }
    });
  }

  getMinimumPointsForPromote(settings) {
    return settings.pointsForPromote.mul(this.currentLadderNum);
  }

  getAutoPromoteCost(rank, settings) {
    let minPeople = this.getMinimumPeopleForPromote(
      this.currentLadderNum,
      settings
    );
    let divisor = Math.max(rank - minPeople + 1, 1);
    return settings.baseGrapesNeededToAutoPromote.div(divisor).floor();
  }

  getMinimumPeopleForPromote(settings) {
    return Math.max(settings.minimumPeopleForPromote, this.currentLadderNum);
  }

  isLadderUnlocked(settings) {
    if (this.rankers.length <= 0) return false;
    let rankerCount = this.rankers.length;
    if (
      rankerCount <
      this.getMinimumPeopleForPromote(this.currentLadderNum, settings)
    )
      return false;
    return (
      this.firstRanker.points.cmp(this.getMinimumPointsForPromote(settings)) >=
      0
    );
  }
}

export default Ladder;
