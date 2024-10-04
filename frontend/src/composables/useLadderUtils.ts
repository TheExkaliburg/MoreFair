import Decimal from "break_infinity.js";
import { computed } from "vue";
import { LadderType, useLadderStore } from "~/store/ladder";
import { RoundType, useRoundStore } from "~/store/round";
import { Ranker } from "~/store/entities/ranker";

let ladder: any;
let round: any;

const getMinimumPointsForPromote = computed<Decimal>(() => {
  return ladder?.state.basePointsToPromote;
});

const getMinimumPeopleForPromote = computed<number>(() => {
  if (round.state.types.has(RoundType.SPECIAL_100)) {
    return round.state.settings.minimumPeopleForPromote;
  }

  return Math.max(
    round.state.settings.minimumPeopleForPromote,
    ladder.state.scaling,
  );
});

const getVinegarThrowCost = computed<Decimal>(() => {
  return round.state.settings.baseVinegarNeededToThrow.mul(
    new Decimal(ladder.state.scaling),
  );
});

const canThrowVinegar = computed<boolean>(() => {
  return (
    ladder.getters.yourRanker !== undefined &&
    ladder.state.rankers[0].growing &&
    ladder.getters.yourRanker.rank !== 1 &&
    ladder.state.rankers[0].points.cmp(getMinimumPointsForPromote.value) >= 0 &&
    ladder.state.rankers.length >= getMinimumPeopleForPromote.value &&
    ladder.getters.yourRanker.vinegar.cmp(getVinegarThrowCost.value) >= 0
  );
});

const getYourPointsNeededToPromote = computed<Decimal>(() => {
  if (ladder.getters.yourRanker === undefined) return new Decimal(Infinity);
  return getPointsNeededToPromote(ladder.getters.yourRanker);
});

const isLadderUnlocked = computed<boolean>(() => {
  if (ladder.state.types.has(LadderType.END)) return false;

  return ladder.state.rankers.length >= getMinimumPeopleForPromote.value;
});

const isLadderPromotable = computed<boolean>(() => {
  return (
    isLadderUnlocked.value &&
    ladder.state.rankers[0].points.cmp(getMinimumPointsForPromote.value) >= 0
  );
});

const canPromote = computed<boolean>(() => {
  if (ladder.getters.yourRanker === undefined || !isLadderPromotable.value)
    return false;
  if (
    ladder.getters.yourRanker.points.cmp(getYourPointsNeededToPromote.value) < 0
  ) {
    return false;
  }

  if (ladder.state.rankers.length < getMinimumPeopleForPromote.value) {
    return false;
  }

  return ladder.getters.yourRanker.rank <= 1;
});

const canBuyAutoPromote = computed<boolean>(() => {
  if (ladder.getters.yourRanker === undefined) return false;
  if (
    ladder.getters.yourRanker.autoPromote ||
    ladder.state.types.has(LadderType.FREE_AUTO) ||
    ladder.state.types.has(LadderType.NO_AUTO)
  ) {
    return false;
  }

  return (
    ladder.getters.yourRanker.grapes.cmp(
      getAutoPromoteCost(ladder.getters.yourRanker.rank),
    ) >= 0 && ladder.state.number >= round.state.autoPromoteLadder
  );
});

const getYourBiasCost = computed<Decimal>(() => {
  if (ladder.getters.yourRanker === undefined) return new Decimal(Infinity);
  const round = useRoundStore();

  let currentUpgrade = ladder.getters.yourRanker.bias;
  if (round.state.number === 300) {
    const ranksBehind =
      round.state.highestAssholeCount - ladder.getters.yourRanker.assholeCount;
    currentUpgrade -= ranksBehind;
  }

  return getNextUpgradeCost(currentUpgrade);
});

const getYourMultiCost = computed<Decimal>(() => {
  if (ladder.getters.yourRanker === undefined) return new Decimal(Infinity);
  const round = useRoundStore();

  let currentUpgrade = ladder.getters.yourRanker.multi;
  if (round.state.number === 300) {
    const ranksBehind =
      round.state.highestAssholeCount - ladder.getters.yourRanker.assholeCount;
    currentUpgrade -= ranksBehind;
  }

  return getNextUpgradeCost(currentUpgrade);
});

const getYourAutoPromoteCost = computed<Decimal>(() => {
  if (ladder.getters.yourRanker === undefined) return new Decimal(Infinity);
  return getAutoPromoteCost(ladder.getters.yourRanker.rank);
});

function getPointsNeededToPromote(ranker: Ranker) {
  // If not enough Players -> Infinity
  if (ladder.state.rankers.length < 2 || ranker === undefined) {
    return new Decimal(Infinity);
  }

  const leadingRanker = ranker.rank === 1 ? ranker : ladder.state.rankers[0];
  const pursuingRanker = ranker.rank === 1 ? ladder.state.rankers[1] : ranker;

  // How many more points does the ranker gain against his pursuer, every second
  const powerDiff = (
    leadingRanker.growing ? leadingRanker.power : new Decimal(0)
  ).sub(pursuingRanker.growing ? pursuingRanker.power : 0);

  // Calculate the needed Point difference, to have f.e. 30seconds of point generation with the difference in power
  const neededPointDiff = powerDiff
    .mul(round.state.settings.manualPromoteWaitTime)
    .abs();
  /*
  if (ranker.rank === 1) {
    console.log("neededPointDiff", neededPointDiff.toString());
    console.log(
      "neededPointTotal",
      useFormatter(pursuingRanker.points.add(neededPointDiff))
    );
  }
  */

  return Decimal.max(
    (ranker.rank === 1 ? pursuingRanker : leadingRanker).points.add(
      neededPointDiff,
    ),
    getMinimumPointsForPromote.value,
  );
}

function getAutoPromoteCost(rank: number): Decimal {
  if (ladder.state.types.has(LadderType.NO_AUTO)) return new Decimal(Infinity);

  const minPeople = getMinimumPeopleForPromote.value;
  const divisor = Math.max(rank - minPeople + 1, 1);
  return round.state.settings.baseGrapesNeededToAutoPromote
    .div(new Decimal(divisor))
    .floor();
}

function getNextUpgradeCost(currentUpgrade: number): Decimal {
  if (currentUpgrade <= 0) currentUpgrade = 0;

  let flatMulti = 1;
  let ladderMulti = 1;
  if (ladder.state.types.has(LadderType.CHEAP)) {
    ladderMulti = 1 - 0.5;
    flatMulti = 1 - 0.5;
  }
  if (ladder.state.types.has(LadderType.EXPENSIVE)) {
    ladderMulti = 1 + 0.5;
    flatMulti = 1 + 0.5;
  }

  let ladderDec = new Decimal(ladder.state.scaling);
  ladderDec = ladderDec.mul(ladderMulti);
  if (ladder.state.types.has(LadderType.CHEAP_2)) {
    ladderDec = ladderDec.mul(ladderMulti);
  }

  let result = ladderDec
    .add(new Decimal(1))
    .pow(currentUpgrade + 1)
    .mul(flatMulti);
  if (ladder.state.types.has(LadderType.CHEAP_2)) {
    result = result.mul(flatMulti);
  }
  return result.round().max(new Decimal(1));
}

export const useLadderUtils = () => {
  ladder = useLadderStore();
  round = useRoundStore();

  return {
    getMinimumPointsForPromote,
    getMinimumPeopleForPromote,
    getVinegarThrowCost,
    canThrowVinegar,
    isLadderPromotable,
    getYourPointsNeededToPromote,
    canPromote,
    canBuyAutoPromote,
    getYourBiasCost,
    getYourMultiCost,
    getYourAutoPromoteCost,
    getPointsNeededToPromote,
    getAutoPromoteCost,
    getNextUpgradeCost,
  };
};
