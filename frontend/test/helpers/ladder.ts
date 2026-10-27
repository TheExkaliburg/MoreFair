import { createPinia, disposePinia, setActivePinia } from "pinia";
import { flushPromises } from "@vue/test-utils";
import Decimal from "break_infinity.js";
import { afterAll, beforeEach, vi } from "vitest";
import { resetUiEffects } from "./uiEffects";
import type { OnTickBody } from "~/composables/useStomp";
import type { LadderData } from "~/store/ladder";
import { LadderType } from "~/store/entities/ladderType";
import { Ranker, type RankerData } from "~/store/entities/ranker";
import { resetEtaCache } from "~/utils/etaCache";

const mocks = vi.hoisted(() => ({
  getLadder: vi.fn<(number: number) => Promise<{ data: LadderData }>>(),
  changeLadder: vi.fn<(number: number) => void>(),
  ticks: new Map<string, (body: OnTickBody) => void>(),
  account: {
    state: {
      accountId: 2,
      highestCurrentLadder: 1,
      settings: { vinegarSplit: 50 },
    },
  },
}));

vi.mock("~/composables/useAPI", () => ({
  useAPI: () => ({ ladder: { getLadder: mocks.getLadder } }),
}));
vi.mock("~/composables/useStomp", () => ({
  useStomp: () => ({
    callbacks: { onTick: "tick", onLadderEvent: "event" },
    wsApi: { ladder: { changeLadder: mocks.changeLadder } },
    addCallback: (
      type: string,
      id: string,
      callback: (body: OnTickBody) => void,
    ) => {
      if (type === "tick") mocks.ticks.set(id, callback);
    },
  }),
}));
vi.mock("~/store/account", () => ({
  useAccountStore: () => mocks.account,
}));
vi.mock("~/store/options", () => ({ useOptionsStore: () => ({}) }));
vi.mock("~/store/chat", () => ({ useChatStore: () => ({}) }));
vi.mock("~/store/grapes", () => ({
  VinegarSuccessType: {},
  useGrapesStore: vi.fn(),
}));

export function ladderSnapshot(
  players: readonly Partial<RankerData>[],
  overrides: Partial<Omit<LadderData, "rankers">> = {},
): LadderData {
  return {
    number: 1,
    scaling: 1,
    types: [LadderType.DEFAULT],
    basePointsToPromote: "1000",
    ...overrides,
    rankers: players.map(
      (player, index) =>
        new Ranker({ accountId: index + 1, rank: index + 1, ...player }),
    ),
  };
}

export function setupLadderTest() {
  const pinia = createPinia();
  // Keep import-time listeners such as ETA invalidation, not prior test listeners.
  const initialTicks = new Map(mocks.ticks);

  beforeEach(async () => {
    setActivePinia(pinia);
    mocks.getLadder.mockReset();
    mocks.changeLadder.mockReset();
    mocks.ticks.clear();
    initialTicks.forEach((callback, id) => mocks.ticks.set(id, callback));
    mocks.account.state.accountId = 2;
    mocks.account.state.highestCurrentLadder = 1;
    mocks.account.state.settings.vinegarSplit = 50;
    resetUiEffects();
    resetEtaCache();

    // Reuse the store so module-level computed helpers keep their reactive dependencies.
    const { useLadderStore } = await import("~/store/ladder");
    Object.assign(useLadderStore().state, {
      rankers: [],
      number: 1,
      scaling: 1,
      types: new Set([LadderType.DEFAULT]),
      basePointsToPromote: new Decimal(0),
    });
  });
  afterAll(() => disposePinia(pinia));

  async function loadLadder(snapshot: LadderData) {
    mocks.getLadder.mockResolvedValue({ data: snapshot });
    const { useLadderStore } = await import("~/store/ladder");
    const ladder = useLadderStore();
    ladder.actions.reset();
    await flushPromises();
    return ladder;
  }

  function tick(delta = 1) {
    if (!mocks.ticks.has("fair_ladder_calculateTick")) {
      throw new Error("Load a ladder before sending a tick");
    }
    mocks.ticks.forEach((callback) => callback({ delta }));
  }

  return { ...mocks, loadLadder, tick };
}
