// @vitest-environment happy-dom
import Decimal from "break_infinity.js";
import { describe, expect, test, vi } from "vitest";
import { ladderSnapshot, setupLadderTest } from "../helpers/ladder";
import { LadderType } from "~/store/entities/ladderType";
import type { RankerData } from "~/store/entities/ranker";

vi.mock("~/store/round", () => ({
  RoundType: {
    SPECIAL_100: "SPECIAL_100",
    APRIL_FOOLS: "APRIL_FOOLS",
    RACE: "RACE",
    FAST: "FAST",
  },
  useRoundStore: () => ({
    state: {
      types: new Set(),
      settings: {
        minimumPeopleForPromote: 2,
        maximumPeopleForPromote: 20,
      },
    },
  }),
}));
const harness = setupLadderTest();
const { account, tick } = harness;

async function loadLadder(
  players: Partial<RankerData>[],
  type = LadderType.DEFAULT,
) {
  const ladder = await harness.loadLadder(
    ladderSnapshot(players, {
      types: [type],
      basePointsToPromote: "100",
    }),
  );
  expect(ladder.state.rankers).toHaveLength(players.length);
  return ladder;
}

describe("ladder tick resource parity with the server", () => {
  test.each([
    [LadderType.DEFAULT, 1, 1],
    [LadderType.DEFAULT, 3, 3],
    [LadderType.BOUNTIFUL, 3, 15],
  ] as const)(
    "%s bottom reward over %i seconds adds %i grapes",
    async (type, delta, addedGrapes) => {
      account.state.accountId = 2;
      const ladder = await loadLadder(
        [{ points: new Decimal(1000) }, {}],
        type,
      );

      tick(delta);

      expect(ladder.getters.yourRanker!.grapes.toNumber()).toBe(addedGrapes);
    },
  );

  test.each([1, 3])(
    "rounds each resource rate before multiplying by %i seconds",
    async (delta) => {
      account.state.accountId = 2;
      const ladder = await loadLadder([
        { points: new Decimal(1000) },
        { grapes: new Decimal(3) },
      ]);

      tick(delta);

      expect(ladder.getters.yourRanker!.vinegar.toNumber()).toBe(delta);
      expect(ladder.getters.yourRanker!.wine.toNumber()).toBe(4 * delta);
    },
  );

  test("earns resources before overtaking first place", async () => {
    account.state.accountId = 2;
    const ladder = await loadLadder([
      { points: new Decimal(100) },
      {
        points: new Decimal(90),
        power: new Decimal(20),
        grapes: new Decimal(10),
        vinegar: new Decimal(100),
        wine: new Decimal(100),
      },
    ]);

    tick(1);

    const player = ladder.getters.yourRanker!;
    expect(player.rank).toBe(1);
    expect(player.vinegar.toNumber()).toBe(105);
    expect(player.wine.toNumber()).toBe(115);
    expect(player.grapes.toNumber()).toBe(10);
  });

  test("decays the leader's resources before being overtaken", async () => {
    account.state.accountId = 1;
    const ladder = await loadLadder([
      {
        points: new Decimal(100),
        multi: 2,
        grapes: new Decimal(10),
        vinegar: new Decimal(100),
        wine: new Decimal(100),
      },
      { points: new Decimal(90), power: new Decimal(20) },
    ]);

    tick(1);

    const player = ladder.getters.yourRanker!;
    expect(player.rank).toBe(2);
    expect(player.vinegar.toNumber()).toBe(99);
    expect(player.wine.toNumber()).toBe(99);
    expect(player.grapes.toNumber()).toBe(12);
  });

  test("uses updated leader points to determine resource decay", async () => {
    account.state.accountId = 1;
    const ladder = await loadLadder([
      {
        points: new Decimal(99),
        vinegar: new Decimal(100),
        wine: new Decimal(100),
      },
      {},
    ]);

    tick(1);

    const player = ladder.getters.yourRanker!;
    expect(player.points.toNumber()).toBe(100);
    expect(player.vinegar.toNumber()).toBe(99);
    expect(player.wine.toNumber()).toBe(99);
  });

  test("does not decay resources on a terminal ladder", async () => {
    account.state.accountId = 1;
    const ladder = await loadLadder(
      [
        {
          points: new Decimal(100),
          vinegar: new Decimal(100),
          wine: new Decimal(100),
        },
        {},
      ],
      LadderType.END,
    );

    tick(1);

    expect(ladder.getters.yourRanker!.vinegar.toNumber()).toBe(100);
    expect(ladder.getters.yourRanker!.wine.toNumber()).toBe(100);
  });

  test("earns resources before receiving passing grapes", async () => {
    account.state.accountId = 2;
    const ladder = await loadLadder([
      { points: new Decimal(1000) },
      {
        points: new Decimal(50),
        multi: 2,
        grapes: new Decimal(3),
      },
      {
        points: new Decimal(49),
        power: new Decimal(10),
        multi: 2,
      },
    ]);

    tick(1);

    const player = ladder.getters.yourRanker!;
    expect(player.rank).toBe(3);
    expect(player.vinegar.toNumber()).toBe(1);
    expect(player.wine.toNumber()).toBe(4);
    expect(player.grapes.toNumber()).toBe(5);
  });
});
