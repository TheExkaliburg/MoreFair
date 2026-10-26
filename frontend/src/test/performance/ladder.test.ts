import { mkdirSync, writeFileSync } from "node:fs";
import { Session } from "node:inspector/promises";
import { join } from "node:path";
import { createPinia, disposePinia, setActivePinia } from "pinia";
import Decimal from "break_infinity.js";
import { afterAll, beforeAll, describe, expect, test, vi } from "vitest";
import { Ranker } from "~/store/entities/ranker";
import { useLadderStore, type LadderData } from "~/store/ladder";
import { LadderType } from "~/store/entities/ladderType";
import { useEta } from "~/composables/useEta";
import { useLadderUtils } from "~/composables/useLadderUtils";
import { useFormatter, useTimeFormatter } from "~/composables/useFormatter";

const { ticks, getLadder, account } = await vi.hoisted(async () => {
  const { ref, computed } = await import("vue");
  vi.stubGlobal("ref", ref);
  vi.stubGlobal("computed", computed);
  vi.stubGlobal("navigator", { language: "en-US" });
  return {
    ticks: new Map<string, (body: { delta: number }) => void>(),
    getLadder: vi.fn<() => Promise<{ data: LadderData }>>(),
    account: {
      state: {
        accountId: 0,
        highestCurrentLadder: 1,
        settings: { vinegarSplit: 50 },
      },
    },
  };
});

vi.mock("~/composables/useAPI", () => ({
  useAPI: () => ({ ladder: { getLadder } }),
}));
vi.mock("~/composables/useStomp", () => ({
  useStomp: () => ({
    callbacks: { onTick: "tick", onLadderEvent: "event" },
    addCallback: (
      type: string,
      id: string,
      callback: (body: { delta: number }) => void,
    ) => {
      if (type === "tick") ticks.set(id, callback);
    },
  }),
}));
vi.mock("~/store/account", () => ({ useAccountStore: () => account }));
vi.mock("~/store/options", () => ({ useOptionsStore: () => ({}) }));
vi.mock("~/store/chat", () => ({ useChatStore: () => ({}) }));
vi.mock("~/store/grapes", () => ({ VinegarSuccessType: {} }));
vi.mock("~/store/round", async (importOriginal) => {
  const module = await importOriginal<typeof import("~/store/round")>();
  const { RoundSettings } = await import("~/store/entities/roundSettings");
  const round = {
    state: {
      types: new Set([module.RoundType.DEFAULT]),
      settings: new RoundSettings({ maximumPeopleForPromote: 100 }),
    },
  };
  return { ...module, useRoundStore: () => round };
});
vi.mock("~/composables/useToasts", () => ({ useToasts: vi.fn() }));
vi.mock("~/composables/useSound", () => ({
  SOUNDS: {},
  useSound: vi.fn(),
}));
vi.mock("~/composables/useTour", () => {
  const tour = {
    flags: {
      value: {
        shownStartup: true,
        shownAutoPromote: true,
        shownMultied: true,
        shownVinegar: true,
      },
    },
  };
  return {
    useAutoPromoteTour: () => tour,
    useBiasedTour: () => tour,
    useMultiedTour: () => tour,
    usePromoteTour: () => tour,
    useStartupTour: () => tour,
    useVinegarTour: () => tour,
  };
});

const pinia = createPinia();
beforeAll(() => setActivePinia(pinia));
afterAll(() => {
  disposePinia(pinia);
  vi.unstubAllGlobals();
});

describe("Ladder calculation and ETA derivatives", () => {
  test.each([100, 1000, 10000])("%i rankers", async (size) => {
    account.state.accountId = Math.floor(size / 2);
    getLadder.mockResolvedValue({
      data: {
        rankers: Array.from(
          { length: size },
          (_, i) =>
            new Ranker({
              accountId: i + 1,
              rank: i + 1,
              points: new Decimal("1e14").mul(size - i),
              power: new Decimal(1e6).mul(1 + (i % 5)),
              bias: 20 + (i % 6),
              multi: 10 + (i % 5),
            }),
        ),
        number: 1,
        scaling: 5,
        types: [LadderType.DEFAULT],
        basePointsToPromote: new Decimal("1e14").mul(size * 0.75).toString(),
      },
    });
    const ladder = useLadderStore();
    ladder.actions.reset();
    await Promise.resolve();
    expect(ladder.state.rankers).toHaveLength(size);
    expect(ticks.has("fair_ladder_calculateTick")).toBe(true);
    expect(ticks.has("fair_eta_cache_reset")).toBe(true);
    const utils = useLadderUtils();
    let timings: Record<string, number> = {};
    let checksum = 0;

    function measure(name: string, run: () => void) {
      const start = performance.now();
      run();
      timings[name] = (timings[name] ?? 0) + performance.now() - start;
    }

    function calculate() {
      const start = performance.now();
      // The store exposes calculateTick through its registered tick callback.
      measure("calculateTick + cache reset", () => {
        for (const tick of ticks.values()) tick({ delta: 1 });
      });
      const rankers = ladder.state.rankers;
      const player = ladder.getters.yourRanker!;
      let promotionEtas: number[] = [];
      let playerEtas: number[] = [];
      let biasCosts: Decimal[] = [];
      let multiCosts: Decimal[] = [];
      measure("toPromote", () => {
        promotionEtas = rankers.map((ranker) => useEta(ranker).toPromote());
      });
      measure("toRanker", () => {
        playerEtas = rankers.map((ranker) => useEta(ranker).toRanker(player));
      });
      measure("upgrade costs + comparisons", () => {
        biasCosts = rankers.map((ranker) =>
          utils.getNextUpgradeCost(ranker.bias),
        );
        multiCosts = rankers.map((ranker) =>
          utils.getNextUpgradeCost(ranker.multi),
        );
        for (let i = 0; i < size; i++) {
          checksum += Number(biasCosts[i]!.cmp(rankers[i]!.points) <= 0);
          checksum += Number(multiCosts[i]!.cmp(rankers[i]!.power) <= 0);
        }
      });
      measure("toPoints", () => {
        for (let i = 0; i < size; i++)
          checksum += Number(useEta(rankers[i]!).toPoints(biasCosts[i]!) < 300);
      });
      measure("toPower", () => {
        for (let i = 0; i < size; i++)
          checksum += Number(useEta(rankers[i]!).toPower(multiCosts[i]!) < 300);
      });
      measure("useFormatter", () => {
        for (const ranker of rankers) {
          checksum += useFormatter(ranker.points).length;
          checksum += useFormatter(ranker.power).length;
          checksum += useFormatter(ranker.getPowerPerSecond()).length;
        }
      });
      measure("useTimeFormatter", () => {
        for (let i = 0; i < size; i++) {
          checksum += useTimeFormatter(promotionEtas[i]!).length;
          checksum += useTimeFormatter(playerEtas[i]!).length;
        }
      });
      measure("ETA color comparisons", () => {
        const playerPromotion = promotionEtas[player.rank - 1]!;
        for (const eta of promotionEtas) {
          const percent = Math.min(
            Math.max((playerPromotion / eta) * 50, 0),
            100,
          );
          checksum += percent < 45 ? 0 : percent < 55 ? 50 : 100;
        }
      });
      timings.total = (timings.total ?? 0) + performance.now() - start;
    }

    for (let i = 0; i < 5; i++) calculate();
    timings = {};
    for (let i = 0; i < 21; i++) calculate();
    expect(Number.isFinite(checksum)).toBe(true);
    expect(checksum).toBeGreaterThan(0);
    // eslint-disable-next-line no-console -- Timing output is the purpose of this test.
    console.table(
      Object.entries(timings).map(([name, elapsed]) => ({
        function: name,
        "ms per entire ladder": (elapsed / 21).toFixed(3),
      })),
    );

    if (import.meta.env.MODE === "profile") {
      const session = new Session();
      session.connect();
      try {
        await session.post("Profiler.enable");
        await session.post("Profiler.start");
        for (let i = 0; i < 21; i++) calculate();
        const { profile } = await session.post("Profiler.stop");
        const output = join(
          process.cwd(),
          "node_modules/.cache/ladder-benchmark",
        );
        mkdirSync(output, { recursive: true });
        const path = join(output, `ladder-${size}.cpuprofile`);
        writeFileSync(path, JSON.stringify(profile));
        console.info(`CPU profile: ${path}`);
      } finally {
        session.disconnect();
      }
    }
  });
});
