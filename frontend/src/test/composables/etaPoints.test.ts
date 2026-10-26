// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { reactive } from "vue";
import Decimal from "break_infinity.js";
import { useEta } from "~/composables/useEta";
import { Ranker } from "~/store/entities/ranker";

const tick = vi.hoisted(() => ({ reset: () => {} }));
vi.mock("~/store/ladder", () => ({
  useLadderStore: () => ladder,
}));
vi.mock("~/composables/useLadderUtils", () => ({
  useLadderUtils: () => ({}),
}));
vi.mock("~/composables/useStomp", () => ({
  useStomp: () => ({
    callbacks: { onTick: [] },
    addCallback: (
      _callbacks: unknown,
      _identifier: string,
      callback: () => void,
    ) => {
      tick.reset = callback;
    },
  }),
}));
vi.mock("~/store/grapes", () => ({ useGrapesStore: vi.fn() }));

const ladder = reactive<{ state: { rankers: Ranker[] } }>({
  state: { rankers: [] },
});

function makeRanker(points: number, rank: number) {
  return new Ranker({
    accountId: rank,
    rank,
    points: new Decimal(points),
    power: new Decimal(10),
    bias: 1,
    multi: 2,
  });
}

beforeEach(() => {
  tick.reset();
  ladder.state.rankers = [100, 80, 80, 40, 10].map((points, i) =>
    makeRanker(points, i + 1),
  );
});

afterEach(() => vi.restoreAllMocks());

describe("points ETA target rank", () => {
  test.each([
    [101, 1],
    [100, 2],
    [81, 2],
    [80, 4],
    [79, 4],
    [41, 4],
    [40, 4],
    [11, 4],
    [Infinity, 1],
  ])(
    "%s points resolve to target rank %s with strict tie handling",
    (target, targetRank) => {
      const ranker = ladder.state.rankers[4]!;
      const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
      useEta(ranker).toPoints(new Decimal(target));
      expect(powerGain.mock.calls).toEqual([[ranker.rank], [targetRank + 1]]);
    },
  );

  test.each([10, 9, 0])(
    "returns zero for an achieved target of %s",
    (target) => {
      const ranker = ladder.state.rankers[4]!;
      const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
      expect(useEta(ranker).toPoints(new Decimal(target))).toBe(0);
      expect(powerGain).not.toHaveBeenCalled();
    },
  );

  test("falls back to one rank above the caller below the whole ladder", () => {
    const ranker = makeRanker(0, 6);
    const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
    useEta(ranker).toPoints(new Decimal(5));
    expect(powerGain.mock.calls).toEqual([[6], [6]]);
  });

  test("preserves the empty-ladder fallback", () => {
    ladder.state.rankers = [];
    const ranker = makeRanker(0, 6);
    const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
    useEta(ranker).toPoints(new Decimal(5));
    expect(powerGain.mock.calls).toEqual([[6], [2]]);
  });

  test("preserves the first-rank caller's linear ETA", () => {
    const ranker = ladder.state.rankers[0]!;
    expect(useEta(ranker).toPoints(new Decimal(150))).toBe(5);
  });

  test("preserves cache hits and invalidation when a tick changes target rank", () => {
    const ranker = ladder.state.rankers[4]!;
    const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
    const eta = useEta(ranker);
    const target = new Decimal(80);
    const original = eta.toPoints(target);
    expect(eta.toPoints(target)).toBe(original);
    expect(powerGain).toHaveBeenCalledTimes(2);

    ladder.state.rankers[1]!.points = new Decimal(90);
    ladder.state.rankers[2]!.points = new Decimal(70);
    tick.reset();
    expect(eta.toPoints(target)).not.toBe(original);
    expect(powerGain.mock.calls.slice(2)).toEqual([[5], [4]]);
  });

  test("matches linear lookup and analytical ETAs across sorted tied ladders", () => {
    for (const size of [1, 2, 5, 10, 50, 128]) {
      ladder.state.rankers = Array.from({ length: size }, (_, i) =>
        makeRanker(10 + Math.floor((size - i) / 3) * 10, i + 1),
      );
      const ranker = makeRanker(0, size + 1);
      const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
      const eta = useEta(ranker);
      const targets = new Set([
        5,
        ...ladder.state.rankers.flatMap((r) => {
          const points = r.points.toNumber();
          return [points - 1, points, points + 1];
        }),
      ]);
      for (const points of targets) {
        powerGain.mockClear();
        const target = new Decimal(points);
        const found = ladder.state.rankers.find(
          (r) => r.points.cmp(target) < 0,
        );
        const targetRank =
          !found || found.rank >= ranker.rank ? ranker.rank - 1 : found.rank;
        const acceleration =
          ((ranker.bias + ranker.rank - 1) * ranker.multi +
            (ranker.bias + targetRank) * ranker.multi) /
          2;
        const speed = ranker.power.toNumber();
        const expected =
          (Math.sqrt(speed * speed + 2 * acceleration * points) - speed) /
          acceleration;

        expect(eta.toPoints(target)).toBeCloseTo(expected, 10);
        expect(powerGain.mock.calls).toEqual([[ranker.rank], [targetRank + 1]]);
      }
    }
  });
});
