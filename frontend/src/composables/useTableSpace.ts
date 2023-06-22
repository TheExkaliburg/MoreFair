import { computed, ComputedRef } from "vue";
import { useOptionsStore } from "~/store/options";
import { useWindowSize } from "~/composables/useWindowSize";

const TOTAL_COLUMNS = 60;

export type TableSpace = {
  rank: number;
  username: number;
  etaToLadder: number;
  etaToYou: number;
  powerGain: number;
  power: number;
  points: number;
  total: number;
};

export type TableSpaceStyles = {
  rank: string;
  username: string;
  etaToLadder: string;
  etaToYou: string;
  powerGain: string;
  power: string;
  points: string;
  total: string;
};

const TableSpaceWeights: TableSpace = {
  rank: 2,
  username: 8,
  power: 4,
  points: 4,
  etaToLadder: 4,
  etaToYou: 4,
  powerGain: 8,
  total: 0,
};

const computedTableSpace = computed<TableSpace>(() => {
  const result: TableSpace = Object.assign({}, TableSpaceWeights);

  const optionsStore = useOptionsStore();

  // IF it's not showing the powerGain, remove it from the total columns
  if (
    !optionsStore.state.ladder.showBiasAndMulti.value &&
    !optionsStore.state.ladder.showPowerGain.value
  ) {
    result.powerGain = 0;
  } else if (
    optionsStore.state.ladder.showBiasAndMulti.value !==
    optionsStore.state.ladder.showPowerGain.value
  ) {
    result.powerGain -= 3;
  }

  // If it's not showing the etas, remove them from the total columns
  if (!optionsStore.state.ladder.showEta.value) {
    result.etaToLadder = 0;
    result.etaToYou = 0;
  }

  let needsSecondRow = true;
  if (
    result.etaToYou === 0 &&
    result.etaToLadder === 0 &&
    result.powerGain === 0
  ) {
    needsSecondRow = false;
  }

  // Less than sm: breakpoint -> need to do it in 2 rows per entry
  if (useWindowSize().isSmallerThanSm && needsSecondRow) {
    const newRow1 = convertTotalWeightsToColumns([
      result.rank,
      result.username,
      result.power,
      result.points,
    ]);
    result.rank = newRow1[0];
    result.username = newRow1[1];
    result.power = newRow1[2];
    result.points = newRow1[3];

    const newRow2 = convertTotalWeightsToColumns([
      result.etaToLadder,
      result.etaToYou,
      result.powerGain,
    ]);
    result.etaToLadder = newRow2[0];
    result.etaToYou = newRow2[1];
    result.powerGain = newRow2[2];
  } else {
    const newWeights = convertTotalWeightsToColumns([
      result.rank,
      result.username,
      result.power,
      result.points,
      result.etaToLadder,
      result.etaToYou,
      result.powerGain,
    ]);

    result.rank = newWeights[0];
    result.username = newWeights[1];
    result.power = newWeights[2];
    result.points = newWeights[3];
    result.etaToLadder = newWeights[4];
    result.etaToYou = newWeights[5];
    result.powerGain = newWeights[6];
  }

  result.total = TOTAL_COLUMNS;

  return result;
});

const computedTableSpaceStyles = computed<TableSpaceStyles>(() => {
  const result: TableSpaceStyles = {
    rank: `grid-column: span ${computedTableSpace.value.rank} / span ${computedTableSpace.value.rank}`,
    username: `grid-column: span ${computedTableSpace.value.username} / span ${computedTableSpace.value.username}`,
    etaToLadder: `grid-column: span ${computedTableSpace.value.etaToLadder} / span ${computedTableSpace.value.etaToLadder}`,
    etaToYou: `grid-column: span ${computedTableSpace.value.etaToYou} / span ${computedTableSpace.value.etaToYou}`,
    powerGain: `grid-column: span ${computedTableSpace.value.powerGain} / span ${computedTableSpace.value.powerGain}`,
    power: `grid-column: span ${computedTableSpace.value.power} / span ${computedTableSpace.value.power}`,
    points: `grid-column: span ${computedTableSpace.value.points} / span ${computedTableSpace.value.points}`,
    total: `grid-template-columns: repeat(${computedTableSpace.value.total}, minmax(0, 1fr))`,
  };
  return result;
});

export const useTableSpaceStyles: () => ComputedRef<TableSpaceStyles> = () => {
  return computedTableSpaceStyles;
};
export const useTableSpace: () => ComputedRef<TableSpace> = () => {
  return computedTableSpace;
};

function convertTotalWeightsToColumns(weights: number[]): number[] {
  const total = weights.reduce((a, b) => a + b, 0);
  const multi = TOTAL_COLUMNS / total;

  const result = weights.map((w) => Math.round(w * multi));
  let newTotal = result.reduce((a, b) => a + b, 0);

  // if the unchecked Total is less than the total columns, add 1 to the smallest element until we reach the total columns
  while (newTotal < TOTAL_COLUMNS) {
    const nonNullResults = result.filter((_, i) => weights[i] !== 0);
    const smallestIndex = result.indexOf(Math.min(...nonNullResults));
    result[smallestIndex]++;
    newTotal++;
  }

  // and vice versa
  while (newTotal > TOTAL_COLUMNS) {
    const nonNullResults = result.filter((_, i) => weights[i] !== 0);
    const biggestIndex = result.indexOf(Math.max(...nonNullResults));
    result[biggestIndex]--;
    newTotal--;
  }

  return result;
}
