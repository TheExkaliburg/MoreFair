// @vitest-environment happy-dom
import { describe, expect, test, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import Decimal from "break_infinity.js";
import { useEta } from "~/composables/useEta";
import { useLadderStore } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";
import { useTimeFormatter } from "~/composables/useFormatter";

vi.mock("~/store/ladder", async () => {
  const { defineStore } = await import("pinia");
  const { reactive } = await import("vue");
  return {
    useLadderStore: defineStore("eta-test", () => ({
      state: reactive<{ rankers: Ranker[] }>({ rankers: [] }),
    })),
  };
});
vi.mock("~/composables/useLadderUtils", () => ({
  useLadderUtils: () => ({}),
}));
vi.mock("~/composables/useStomp", () => ({
  useStomp: () => ({ addCallback: vi.fn(), callbacks: { onTick: [] } }),
}));
vi.mock("~/store/grapes", () => ({ useGrapesStore: vi.fn() }));

setActivePinia(createPinia());

describe("a 2 person ladder", () => {
  const ladderStore = useLadderStore();
  ladderStore.state.rankers = [
    new Ranker({
      accountId: 1,
      rank: 1,
      points: new Decimal(2),
      power: new Decimal(1),
      bias: 0,
      multi: 1,
    }),
    new Ranker({
      accountId: 2,
      rank: 2,
      points: new Decimal(1),
      power: new Decimal(1),
      bias: 1,
      multi: 3,
    }),
  ];

  test("example", () => {
    const eta = useEta(ladderStore.state.rankers[0]!);
    expect(eta.toPoints(new Decimal(6))).toBe(4);
  });

  test("formats a large finite points ETA without exceeding Date limits", () => {
    const eta = useEta(ladderStore.state.rankers[0]!);
    const seconds = eta.toPoints(new Decimal(1e40));
    expect(Number.isFinite(seconds)).toBe(true);
    expect(seconds).toBeGreaterThan(8_640_000_000_000);
    expect(() => useTimeFormatter(seconds)).not.toThrow();
  });
});
