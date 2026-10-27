// @vitest-environment happy-dom
import { beforeEach, describe, expect, test, vi } from "vitest";
import Decimal from "break_infinity.js";
import { useLadderStore } from "~/store/ladder";
import { hasLadderType, LadderType } from "~/store/entities/ladderType";
import { useRoundStore } from "~/store/round";
import { Ranker } from "~/store/entities/ranker";
import { useLadderUtils } from "~/composables/useLadderUtils";

vi.mock("~/store/ladder", async () => {
  const types = await import("~/store/entities/ladderType");
  const { reactive } = await import("vue");
  const store = reactive({
    state: {
      types: new Set<LadderType>(),
      scaling: 1,
      basePointsToPromote: 0,
      rankers: [],
    },
  });
  return { ...types, useLadderStore: () => store };
});

vi.mock("~/store/round", () => {
  const store = {
    state: {
      types: new Set(),
      settings: {
        minimumPeopleForPromote: 1,
        maximumPeopleForPromote: 1,
      },
    },
  };
  return {
    RoundType: {
      SPECIAL_100: "SPECIAL_100",
      APRIL_FOOLS: "APRIL_FOOLS",
      RACE: "RACE",
      FAST: "FAST",
    },
    useRoundStore: () => store,
  };
});

describe("terminal ladder types", () => {
  beforeEach(() => {
    const ladder = useLadderStore();
    const round = useRoundStore();
    round.state.settings.minimumPeopleForPromote = 1;
    round.state.settings.maximumPeopleForPromote = 1;
    ladder.state.scaling = 1;
    ladder.state.basePointsToPromote = new Decimal(10);
    ladder.state.rankers = [new Ranker({ rank: 1, points: new Decimal(1000) })];
  });

  test.each([
    [LadderType.DEFAULT, true],
    [LadderType.END, false],
    [LadderType.VOID, false],
  ] as const)("%s promotion eligibility is %s", (type, promotable) => {
    const ladder = useLadderStore();
    const utils = useLadderUtils();
    ladder.state.types.clear();
    ladder.state.types.add(type);

    expect(utils.isLadderPromotable.value).toBe(promotable);
  });

  test("VOID implies END without changing exact type identity", () => {
    const types = new Set([LadderType.VOID]);

    expect(hasLadderType(types, LadderType.END)).toBe(true);
    expect(hasLadderType(types, LadderType.VOID)).toBe(true);
    expect(types.has(LadderType.END)).toBe(false);
    expect(hasLadderType(new Set([LadderType.END]), LadderType.VOID)).toBe(
      false,
    );
    expect(hasLadderType(types, LadderType.ASSHOLE)).toBe(false);
    expect(hasLadderType(new Set(), LadderType.END)).toBe(false);
  });

  test("promotion eligibility reacts to terminal types being added and removed", () => {
    const ladder = useLadderStore();
    const utils = useLadderUtils();
    ladder.state.types.clear();
    ladder.state.types.add(LadderType.DEFAULT);
    expect(utils.isLadderPromotable.value).toBe(true);

    ladder.state.types.add(LadderType.VOID);
    expect(utils.isLadderPromotable.value).toBe(false);

    ladder.state.types.delete(LadderType.VOID);
    expect(utils.isLadderPromotable.value).toBe(true);
  });
});
