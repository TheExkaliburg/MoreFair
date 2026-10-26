// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { createPinia, disposePinia, setActivePinia } from "pinia";
import { flushPromises } from "@vue/test-utils";
import Decimal from "break_infinity.js";
import { useEta } from "~/composables/useEta";
import { useLadderStore, type LadderData } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";
import { LadderType } from "~/store/entities/ladderType";
import {
  computedTrigger,
  etaRankerCache,
  etaPointsCache,
  etaPowerCache,
  resetEtaCache,
} from "~/utils/etaCache";

const mocks = vi.hoisted(() => ({
  getLadder: vi.fn<() => Promise<{ data: LadderData }>>(),
  changeLadder: vi.fn(),
  ticks: new Map<string, () => void>(),
}));

vi.mock("~/composables/useAPI", () => ({
  useAPI: () => ({ ladder: { getLadder: mocks.getLadder } }),
}));
vi.mock("~/composables/useStomp", () => ({
  useStomp: () => ({
    callbacks: { onTick: "tick", onLadderEvent: "event" },
    wsApi: { ladder: { changeLadder: mocks.changeLadder } },
    addCallback: (type: string, id: string, callback: () => void) => {
      if (type === "tick") mocks.ticks.set(id, callback);
    },
  }),
}));
vi.mock("~/store/account", () => ({
  useAccountStore: () => ({
    state: { accountId: 2, highestCurrentLadder: 1 },
  }),
}));
vi.mock("~/store/options", () => ({ useOptionsStore: () => ({}) }));
vi.mock("~/store/chat", () => ({ useChatStore: () => ({}) }));
vi.mock("~/store/round", () => ({ useRoundStore: () => ({}) }));
vi.mock("~/store/grapes", () => ({
  VinegarSuccessType: {},
  useGrapesStore: vi.fn(),
}));
vi.mock("~/composables/useLadderUtils", () => ({
  useLadderUtils: () => ({}),
}));
vi.mock("~/composables/useFormatter", () => ({ useFormatter: vi.fn() }));
vi.mock("~/composables/useToasts", () => ({ useToasts: vi.fn() }));
vi.mock("~/composables/useSound", () => ({
  SOUNDS: {},
  useSound: vi.fn(),
}));
vi.mock("~/composables/useTour", () => ({
  usePromoteTour: () => ({
    flags: { value: { shownPromoted: true, shownVinegar: true } },
  }),
}));

let pinia: ReturnType<typeof createPinia>;
const target = new Decimal(1000);

function snapshot(number: number, updated = false): { data: LadderData } {
  return {
    data: {
      number,
      rankers: [
        new Ranker({
          accountId: 1,
          rank: 1,
          points: new Decimal(updated ? 200 : 100),
          power: new Decimal(10),
        }),
        new Ranker({
          accountId: 2,
          rank: 2,
          points: new Decimal(updated ? 30 : 10),
          power: new Decimal(updated ? 20 : 10),
          bias: updated ? 3 : 1,
          multi: updated ? 4 : 2,
        }),
      ],
      scaling: 1,
      types: [LadderType.DEFAULT],
      basePointsToPromote: "1000",
    },
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
  pinia = createPinia();
  setActivePinia(pinia);
  mocks.getLadder.mockReset();
  mocks.changeLadder.mockClear();
  resetEtaCache();
  mocks.getLadder.mockResolvedValueOnce(snapshot(1));
  useLadderStore().actions.init();
  await flushPromises();
});

afterEach(() => disposePinia(pinia));

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
    reset!();
    expectEmptyCaches();
    expect(computedTrigger.value).not.toBe(trigger);
  });
});
