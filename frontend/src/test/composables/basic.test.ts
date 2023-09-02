import { describe, expect, test } from "vitest";
import Decimal from "break_infinity.js";
import { useEta } from "~/composables/useEta";
import { useLadderStore } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";

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
    const eta = useEta(ladderStore.state.rankers[0]);
    expect(eta.toPoints(new Decimal(6))).toBe(4);
  });
});
