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

describe("ETA calculations and caching", () => {
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

  test("finds a deep target in logarithmic comparisons on a large ladder", () => {
    const size = 10000;
    ladder.state.rankers = Array.from({ length: size }, (_, i) =>
      makeRanker((size - i) * 100, i + 1),
    );
    const pointValues = new Set(
      ladder.state.rankers.map((ranker) => ranker.points),
    );
    const comparisons = vi.spyOn(Decimal.prototype, "cmp");
    useEta(ladder.state.rankers[size - 1]!).toPoints(new Decimal(150));
    const lookupComparisons = comparisons.mock.contexts.filter(
      (value) => value instanceof Decimal && pointValues.has(value),
    ).length;
    expect(lookupComparisons).toBeGreaterThan(0);
    expect(lookupComparisons).toBeLessThanOrEqual(
      Math.ceil(Math.log2(size)) + 1,
    );
  });

  describe.each(["toPoints", "toPower"] as const)(
    "%s value cache",
    (method) => {
      test("reuses equal targets across Decimal instances and composable calls", () => {
        const ranker = ladder.state.rankers[4]!;
        const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
        const original = useEta(ranker)[method](new Decimal(1200));
        const calculations = powerGain.mock.calls.length;
        expect(calculations).toBeGreaterThan(0);

        expect(useEta(ranker)[method](new Decimal("1.2e3"))).toBe(original);
        expect(useEta(ranker)[method](new Decimal("1200.0"))).toBe(original);
        expect(powerGain).toHaveBeenCalledTimes(calculations);
      });

      test("keeps distinct targets beyond native-number range separate", () => {
        const ranker = ladder.state.rankers[4]!;
        const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
        const eta = useEta(ranker);
        const first = new Decimal("1e400");
        const second = new Decimal("2e400");
        expect(first.toNumber()).toBe(second.toNumber());
        eta[method](first);
        const calculations = powerGain.mock.calls.length;

        eta[method](second);
        expect(powerGain).toHaveBeenCalledTimes(calculations * 2);
        eta[method](new Decimal("1e400"));
        expect(powerGain).toHaveBeenCalledTimes(calculations * 2);
      });

      test("does not round adjacent Decimal mantissas into the same key", () => {
        const ranker = ladder.state.rankers[4]!;
        const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
        const eta = useEta(ranker);
        const first = Decimal.fromMantissaExponent(1.0000000000000002, 20);
        const second = Decimal.fromMantissaExponent(1.0000000000000004, 20);
        expect(first.cmp(second)).not.toBe(0);
        eta[method](first);
        const calculations = powerGain.mock.calls.length;

        eta[method](second);
        expect(powerGain).toHaveBeenCalledTimes(calculations * 2);
      });

      test("keeps results separate for different players", () => {
        const first = ladder.state.rankers[4]!;
        const second = ladder.state.rankers[3]!;
        const firstPower = vi.spyOn(first, "getPowerPerSecond");
        const secondPower = vi.spyOn(second, "getPowerPerSecond");
        const firstResult = useEta(first)[method](new Decimal(1200));
        const secondResult = useEta(second)[method](new Decimal(1200));

        expect(firstPower).toHaveBeenCalled();
        expect(secondPower).toHaveBeenCalled();
        expect(secondResult).not.toBe(firstResult);
      });

      test("recalculates the same target after a tick", () => {
        const ranker = ladder.state.rankers[4]!;
        const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
        const eta = useEta(ranker);
        const original = eta[method](new Decimal(1200));
        const calculations = powerGain.mock.calls.length;
        ranker.points = new Decimal(20);
        ranker.power = new Decimal(20);
        tick.reset();

        expect(eta[method](new Decimal(1200))).not.toBe(original);
        expect(powerGain).toHaveBeenCalledTimes(calculations * 2);
      });

      test("reuses cached results for non-growing players", () => {
        const ranker = ladder.state.rankers[4]!;
        ranker.growing = false;
        const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
        const eta = useEta(ranker);
        const original = eta[method](new Decimal(1200));
        const calculations = powerGain.mock.calls.length;
        expect(calculations).toBeGreaterThan(0);

        expect(eta[method](new Decimal(1200))).toBe(original);
        expect(powerGain).toHaveBeenCalledTimes(calculations);
      });
    },
  );

  describe("ETA cache isolation and calculation counts", () => {
    test("keeps points and power results separate for the same player and target", () => {
      const ranker = ladder.state.rankers[4]!;
      const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
      const eta = useEta(ranker);
      const pointsEta = eta.toPoints(new Decimal(1200));
      const powerEta = eta.toPower(new Decimal(1200));
      expect(pointsEta).not.toBe(powerEta);
      expect(powerGain).toHaveBeenCalledTimes(3);

      expect(eta.toPoints(new Decimal(1200))).toBe(pointsEta);
      expect(eta.toPower(new Decimal(1200))).toBe(powerEta);
      expect(powerGain).toHaveBeenCalledTimes(3);
    });

    test("reuses a cached zero power ETA", () => {
      const ranker = ladder.state.rankers[4]!;
      const powerGain = vi.spyOn(ranker, "getPowerPerSecond");
      const eta = useEta(ranker);
      expect(eta.toPower(new Decimal(10))).toBe(0);
      expect(eta.toPower(new Decimal(10))).toBe(0);
      expect(powerGain).toHaveBeenCalledOnce();
    });

    test.each([100, 1000])(
      "calculates fresh equal targets only once per player across %s rankers",
      (size) => {
        ladder.state.rankers = Array.from({ length: size }, (_, i) =>
          makeRanker((size - i) * 100, i + 1),
        );
        const powerGain = vi.spyOn(Ranker.prototype, "getPowerPerSecond");
        for (let pass = 0; pass < 3; pass++) {
          for (const ranker of ladder.state.rankers) {
            const eta = useEta(ranker);
            eta.toPoints(ranker.points.add(10));
            eta.toPower(ranker.power.add(10));
          }
        }
        expect(powerGain).toHaveBeenCalledTimes(size * 3 - 2);
      },
    );
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
