// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { mount, type VueWrapper } from "@vue/test-utils";
import { computed, nextTick, reactive, ref } from "vue";
import Decimal from "break_infinity.js";
import LadderWindowTableRow from "~/components/ladder/LadderWindowTableRow.vue";
import { Ranker } from "~/store/entities/ranker";
import { EtaColorType } from "~/store/options";

vi.mock("~/store/ladder", () => ({
  useLadderStore: () => ladder,
}));
vi.mock("~/store/options", () => ({
  EtaColorType: { OFF: "Off", COLORS: "3-Colors", GRADIENT: "Gradient" },
  useOptionsStore: () => options,
}));
vi.mock("~/composables/useFormatter", () => ({
  useFormatter: (value: Decimal) => formatNumber(value),
  useTimeFormatter: (value: number) => formatTime(value),
}));
vi.mock("~/composables/useEta", () => ({
  useEta: () => eta,
}));
vi.mock("~/composables/useLadderUtils", () => ({
  useLadderUtils: () => ({ getNextUpgradeCost: upgradeCost }),
}));
vi.mock("~/composables/useTableSpace", () => ({
  useTableSpace: () =>
    computed(() => ({
      etaToLadder: options.state.ladder.showEta.value ? 4 : 0,
      etaToYou: options.state.ladder.showEta.value ? 4 : 0,
      powerGain:
        options.state.ladder.showPowerGain.value ||
        options.state.ladder.showBiasAndMulti.value
          ? 8
          : 0,
    })),
  useTableSpaceStyles: () => ref({ total: "display: grid" }),
}));

const formatNumber = vi.fn((value: Decimal) => value.toString());
const formatTime = vi.fn((value: number) => `${value}s`);
const upgradeCost = vi.fn(() => new Decimal(1000));
const eta = {
  toPromote: vi.fn(() => 600),
  toRanker: vi.fn(() => 300),
  toPoints: vi.fn(() => 120),
  toPower: vi.fn(() => 600),
};
const ladder = {
  getters: { yourRanker: new Ranker({ accountId: 1, rank: 1 }) },
};
const options = reactive({
  state: {
    general: { showAssholePoints: { value: false } },
    ladder: {
      showEta: { value: true },
      etaColors: { value: EtaColorType.GRADIENT },
      biasMultiColors: { value: true },
      showPowerGain: { value: true },
      showBiasAndMulti: { value: true },
      followOwnRanker: { value: false },
    },
  },
});

const observers: TestIntersectionObserver[] = [];

class TestIntersectionObserver implements IntersectionObserver {
  readonly root: Element | Document | null;
  readonly rootMargin: string;
  readonly thresholds: readonly number[];
  targets = new Set<Element>();
  observe = vi.fn((target: Element) => {
    this.targets.add(target);
  });
  unobserve = vi.fn((target: Element) => {
    this.targets.delete(target);
  });
  disconnect = vi.fn();
  takeRecords = () => [];

  constructor(
    private callback: IntersectionObserverCallback,
    options: IntersectionObserverInit = {},
  ) {
    this.root = options.root ?? null;
    this.rootMargin = options.rootMargin ?? "0px";
    this.thresholds = Array.isArray(options.threshold)
      ? options.threshold
      : [options.threshold ?? 0];
    observers.push(this);
  }

  emit(target: Element, isIntersecting: boolean) {
    const rect = target.getBoundingClientRect();
    this.callback(
      [
        {
          target,
          isIntersecting,
          intersectionRatio: isIntersecting ? 1 : 0,
          time: performance.now(),
          boundingClientRect: rect,
          intersectionRect: rect,
          rootBounds: null,
        },
      ],
      this,
    );
  }
}

let wrapper: VueWrapper;
let container: HTMLDivElement;

function ranker(points = 10) {
  return new Ranker({
    accountId: 2,
    username: "Player",
    rank: 2,
    points: new Decimal(points),
    power: new Decimal(20),
    bias: 1,
    multi: 2,
  });
}

function observer() {
  const current = observers.findLast((candidate) =>
    candidate.targets.has(wrapper.element),
  );
  if (!current) throw new Error("Row has no intersection observer");
  return current;
}

async function setVisible(visible: boolean) {
  observer().emit(wrapper.element, visible);
  await nextTick();
}

function expectNoDisplayWork() {
  for (const callback of [
    formatNumber,
    formatTime,
    upgradeCost,
    ...Object.values(eta),
  ]) {
    expect(callback).not.toHaveBeenCalled();
  }
}

beforeEach(async () => {
  vi.clearAllMocks();
  observers.length = 0;
  vi.stubGlobal("IntersectionObserver", TestIntersectionObserver);
  options.state.ladder.showEta.value = true;
  options.state.ladder.etaColors.value = EtaColorType.GRADIENT;
  options.state.ladder.biasMultiColors.value = true;
  options.state.ladder.showPowerGain.value = true;
  options.state.ladder.showBiasAndMulti.value = true;
  container = document.createElement("div");
  document.body.appendChild(container);
  wrapper = mount(LadderWindowTableRow, {
    props: { ranker: ranker(), index: 1, container },
    attachTo: container,
  });
  await nextTick();
});

afterEach(() => {
  wrapper?.unmount();
  container?.remove();
  vi.unstubAllGlobals();
});

describe("ladder row visibility", () => {
  test("prepares rows within a 200px vertical buffer", () => {
    expect(observer().rootMargin).toBe("200px 0px");
  });

  test("observes the mounted row inside its scroll container", async () => {
    expect(observer().root).toBe(container);
    const replacement = document.createElement("div");
    await wrapper.setProps({ container: replacement });
    expect(observer().root).toBe(replacement);
  });

  test("does no display calculations while offscreen, including after a tick", async () => {
    expectNoDisplayWork();
    await wrapper.setProps({ ranker: ranker(50) });
    expectNoDisplayWork();
    expect(wrapper.get(".points").text()).toBe("");
    expect(wrapper.get(".power").text()).toBe("");
  });

  test("calculates current values when the row becomes visible", async () => {
    await wrapper.setProps({ ranker: ranker(50) });
    await setVisible(true);
    expect(wrapper.get(".points").text()).toBe("50");
    expect(wrapper.get(".power").text()).toBe("20");
    expect(eta.toPromote).toHaveBeenCalled();
    expect(eta.toRanker).toHaveBeenCalledWith(ladder.getters.yourRanker);
    expect(eta.toPoints).toHaveBeenCalled();
    expect(eta.toPower).toHaveBeenCalled();
    expect(wrapper.find(".text-eta-mid").exists()).toBe(true);
    expect(wrapper.find(".text-eta-worst").exists()).toBe(true);
  });

  test("stops work when leaving view and refreshes on reentry", async () => {
    await setVisible(true);
    await setVisible(false);
    vi.clearAllMocks();
    await wrapper.setProps({ ranker: ranker(75) });
    expectNoDisplayWork();
    await setVisible(true);
    expect(wrapper.get(".points").text()).toBe("75");
    expect(formatNumber).toHaveBeenCalled();
    expect(eta.toPromote).toHaveBeenCalled();
  });

  test("preserves grid cells while their offscreen contents are empty", async () => {
    const cells = Array.from(wrapper.element.children);
    await setVisible(true);
    await setVisible(false);
    expect(Array.from(wrapper.element.children)).toEqual(cells);
    expect(wrapper.get(".rank").text()).toBe("2");
    expect(wrapper.text()).toContain("Player");
  });

  test("skips upgrade calculations when their colors are disabled", async () => {
    options.state.ladder.biasMultiColors.value = false;
    await setVisible(true);
    expect(upgradeCost).not.toHaveBeenCalled();
    expect(eta.toPoints).not.toHaveBeenCalled();
    expect(eta.toPower).not.toHaveBeenCalled();
    expect(eta.toPromote).toHaveBeenCalled();
    expect(wrapper.text()).toContain("+01");
    expect(wrapper.text()).toContain("x02");
  });

  test("does no ETA work with ETA columns and both color options disabled", async () => {
    options.state.ladder.showEta.value = false;
    options.state.ladder.etaColors.value = EtaColorType.OFF;
    options.state.ladder.biasMultiColors.value = false;
    await setVisible(true);
    for (const callback of Object.values(eta)) {
      expect(callback).not.toHaveBeenCalled();
    }
    expect(formatTime).not.toHaveBeenCalled();
    expect(formatNumber).toHaveBeenCalled();
  });

  test("keeps promotion coloring working without ETA columns", async () => {
    options.state.ladder.showEta.value = false;
    options.state.ladder.biasMultiColors.value = false;
    await setVisible(true);
    expect(eta.toPromote).toHaveBeenCalledTimes(2);
    expect(eta.toRanker).not.toHaveBeenCalled();
    expect(formatTime).not.toHaveBeenCalled();
    expect(wrapper.get(".points").attributes("style")).toContain("-50s");
  });
});
