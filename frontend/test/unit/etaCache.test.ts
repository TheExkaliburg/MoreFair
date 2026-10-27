// @vitest-environment happy-dom
import { beforeEach, describe, expect, test, vi } from "vitest";
import { flushPromises } from "@vue/test-utils";
import Decimal from "break_infinity.js";
import { ladderSnapshot, setupLadderTest } from "../helpers/ladder";
import { useEta } from "~/composables/useEta";
import { useLadderStore, type LadderData } from "~/store/ladder";
import {
  computedTrigger,
  etaRankerCache,
  etaPointsCache,
  etaPowerCache,
} from "~/utils/etaCache";

vi.mock("~/store/round", () => ({ useRoundStore: () => ({}) }));
vi.mock("~/composables/useLadderUtils", () => ({
  useLadderUtils: () => ({}),
}));
vi.mock("~/composables/useFormatter", () => ({ useFormatter: vi.fn() }));

const mocks = setupLadderTest();
const target = new Decimal(1000);

function snapshot(number: number, updated = false): { data: LadderData } {
  return {
    data: ladderSnapshot(
      [
        {
          points: new Decimal(updated ? 200 : 100),
          power: new Decimal(10),
        },
        {
          points: new Decimal(updated ? 30 : 10),
          power: new Decimal(updated ? 20 : 10),
          bias: updated ? 3 : 1,
          multi: updated ? 4 : 2,
        },
      ],
      { number },
    ),
  };
}

function readEtas() {
  const ladder = useLadderStore();
  const eta = useEta(ladder.state.rankers[1]!);
  return [
    eta.toRanker(ladder.state.rankers[0]!),
    eta.toPoints(target),
    eta.toPower(target),
  ];
}

function expectEmptyCaches() {
  expect(etaRankerCache.size).toBe(0);
  expect(etaPointsCache.size).toBe(0);
  expect(etaPowerCache.size).toBe(0);
}

beforeEach(async () => {
  await mocks.loadLadder(snapshot(1).data);
});

describe("ETA cache invalidation", () => {
  test.each(["switch", "refresh"] as const)(
    "clears all caches when a ladder %s finishes loading, before any tick",
    async (action) => {
      const ladder = useLadderStore();
      const previous = readEtas();
      expect(etaRankerCache.size).toBeGreaterThan(0);
      expect(etaPointsCache.size).toBeGreaterThan(0);
      expect(etaPowerCache.size).toBeGreaterThan(0);
      const trigger = computedTrigger.value;
      const number = action === "switch" ? 2 : 1;
      let finishLoad!: (value: { data: LadderData }) => void;
      mocks.getLadder.mockReturnValueOnce(
        new Promise((resolve) => {
          finishLoad = resolve;
        }),
      );

      if (action === "switch") ladder.actions.changeLadder(number);
      else ladder.actions.reset();

      expect(readEtas()).toEqual(previous);
      finishLoad(snapshot(number, true));
      await flushPromises();

      expect(ladder.state.number).toBe(number);
      expect(mocks.getLadder).toHaveBeenLastCalledWith(number);
      expectEmptyCaches();
      expect(computedTrigger.value).not.toBe(trigger);
      const updated = readEtas();
      for (let i = 0; i < previous.length; i++) {
        expect(updated[i]).not.toBe(previous[i]);
        expect(Number.isFinite(updated[i])).toBe(true);
      }
      expect(readEtas()).toEqual(updated);
    },
  );

  test("continues clearing all caches on ticks", () => {
    readEtas();
    const trigger = computedTrigger.value;
    const reset = mocks.ticks.get("fair_eta_cache_reset");
    expect(reset).toBeDefined();
    reset!({ delta: 1 });
    expectEmptyCaches();
    expect(computedTrigger.value).not.toBe(trigger);
  });
});
