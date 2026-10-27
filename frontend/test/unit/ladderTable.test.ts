// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { mount, type VueWrapper } from "@vue/test-utils";
import { nextTick, reactive, ref } from "vue";
import { useElementSize } from "@vueuse/core";
import LadderWindowTable from "~/components/ladder/LadderWindowTable.vue";
import { Ranker } from "~/store/entities/ranker";

vi.mock("~/store/ladder", () => ({
  useLadderStore: () => ladder,
}));
vi.mock("~/store/options", () => ({
  useOptionsStore: () => options,
}));
vi.mock("~/composables/useLang", () => ({
  useLang: () => (key: string) => key,
}));
vi.mock("~/composables/useTableSpace", () => ({
  useTableSpaceStyles: () => ref({ total: "display: grid" }),
}));
vi.mock("@vueuse/core", () => ({
  useElementSize: vi.fn(),
}));
vi.mock("~/components/ladder/LadderWindowTableRow.vue", () => ({
  default: {
    props: ["ranker", "index", "container"],
    template:
      '<div class="ranker-row" :data-id="ranker.accountId" :data-index="index">{{ ranker.username }}</div>',
  },
}));

const ladder = reactive({
  state: { number: 1, rankers: [] as Ranker[] },
  getters: {
    shownRankers: [] as Ranker[],
    yourRanker: undefined as Ranker | undefined,
  },
});
const options = reactive({
  state: {
    ladder: {
      showEta: { value: true },
      showBiasAndMulti: { value: true },
      showPowerGain: { value: true },
      followOwnRanker: { value: false },
    },
  },
});
const viewportHeight = ref(400);
const headerHeight = ref(24);
const rowHeight = ref(20);
let wrapper: VueWrapper;

function makeRankers(size: number) {
  return Array.from(
    { length: size },
    (_, index) =>
      new Ranker({
        accountId: index + 1,
        rank: index + 1,
        username: `Player ${index + 1}`,
      }),
  );
}

function mountTable() {
  wrapper = mount(LadderWindowTable);
}

function mountedIds() {
  return wrapper
    .findAll(".ranker-row")
    .map((row) => Number(row.attributes("data-id")));
}

async function scrollTo(top: number) {
  wrapper.element.scrollTop = top;
  await wrapper.trigger("scroll");
}

beforeEach(() => {
  ladder.state.rankers = makeRankers(1000);
  ladder.getters.shownRankers = ladder.state.rankers;
  ladder.getters.yourRanker = ladder.state.rankers[499];
  options.state.ladder.followOwnRanker.value = false;
  viewportHeight.value = 400;
  headerHeight.value = 24;
  rowHeight.value = 20;
  const size = (height: typeof rowHeight) => ({
    height,
    width: ref(800),
    stop: vi.fn(),
  });
  vi.mocked(useElementSize)
    .mockReturnValueOnce(size(viewportHeight))
    .mockReturnValueOnce(size(headerHeight))
    .mockReturnValueOnce(size(rowHeight));
});

afterEach(() => {
  wrapper?.unmount();
  vi.mocked(useElementSize).mockReset();
});

describe("ladder table virtualization", () => {
  test("mounts only the viewport and buffer without changing the simulation", () => {
    ladder.state.rankers = makeRankers(10000);
    ladder.getters.shownRankers = ladder.state.rankers;
    mountTable();
    expect(mountedIds().length).toBeLessThan(50);
    expect(mountedIds()).toContain(1);
    expect(mountedIds()).not.toContain(10000);
    expect(ladder.state.rankers).toHaveLength(10000);
  });

  test("mounts scrolled rows and retains the sticky first row", async () => {
    mountTable();
    await scrollTo(10024);
    expect(mountedIds()).toContain(501);
    expect(mountedIds()).toContain(1);
    expect(mountedIds()).not.toContain(2);
    expect(mountedIds().length).toBeLessThan(50);
    expect(
      wrapper.get(".sticky").find(".ranker-row").attributes("data-id"),
    ).toBe("1");
    expect(wrapper.get(".sticky").classes()).toContain("border-b-1");
    expect(wrapper.find('[data-id="501"]').attributes("data-index")).toBe(
      "500",
    );
  });

  test("reaches the final row and preserves the full scroll height", async () => {
    mountTable();
    await scrollTo(19624);
    expect(mountedIds()).toContain(1000);
    const spacers = wrapper.findAll<HTMLDivElement>('[style*="height:"]');
    const spacerHeight = spacers.reduce(
      (total, spacer) => total + parseFloat(spacer.element.style.height),
      0,
    );
    expect(spacerHeight + mountedIds().length * rowHeight.value).toBe(20000);
  });

  test("uses measured responsive row and viewport heights", async () => {
    mountTable();
    await scrollTo(10024);
    rowHeight.value = 44;
    headerHeight.value = 52;
    viewportHeight.value = 600;
    await nextTick();
    expect(mountedIds()).toContain(230);
    expect(mountedIds()).not.toContain(501);
    expect(mountedIds().length).toBeLessThan(40);
  });

  test("centers an initially unmounted player when following is enabled", async () => {
    options.state.ladder.followOwnRanker.value = true;
    mountTable();
    await nextTick();
    expect(wrapper.element.scrollTop).toBe(9814);
    expect(mountedIds()).toContain(500);
  });

  test("follows the player's new position after a tick", async () => {
    options.state.ladder.followOwnRanker.value = true;
    mountTable();
    await nextTick();
    const rankers = [...ladder.state.rankers];
    const player = rankers.splice(499, 1)[0]!;
    rankers.splice(799, 0, player);
    ladder.state.rankers = rankers;
    ladder.getters.shownRankers = rankers;
    await nextTick();
    await nextTick();
    expect(wrapper.element.scrollTop).toBe(15814);
    expect(mountedIds()).toContain(500);
  });

  test("allows manual scrolling when following is disabled", async () => {
    mountTable();
    expect(wrapper.classes()).toContain("overflow-y-auto");
    await scrollTo(12000);
    ladder.getters.shownRankers = [...ladder.state.rankers];
    await nextTick();
    expect(wrapper.element.scrollTop).toBe(12000);
    expect(mountedIds()).not.toContain(500);
  });

  test("locks manual scrolling only while following a displayed player", async () => {
    mountTable();
    options.state.ladder.followOwnRanker.value = true;
    await nextTick();
    expect(wrapper.classes()).toContain("overflow-y-hidden");
    expect(wrapper.classes()).not.toContain("overflow-y-auto");
    expect(wrapper.element.scrollTop).toBe(9814);

    options.state.ladder.followOwnRanker.value = false;
    await nextTick();
    expect(wrapper.classes()).toContain("overflow-y-auto");
    expect(wrapper.classes()).not.toContain("overflow-y-hidden");
    await scrollTo(12000);
    expect(wrapper.element.scrollTop).toBe(12000);
  });

  test("keeps manual scrolling available when there is no player to follow", async () => {
    options.state.ladder.followOwnRanker.value = true;
    ladder.getters.yourRanker = undefined;
    mountTable();
    await nextTick();
    expect(wrapper.classes()).toContain("overflow-y-auto");

    ladder.getters.yourRanker = ladder.state.rankers[499];
    await nextTick();
    expect(wrapper.classes()).toContain("overflow-y-hidden");

    ladder.getters.shownRankers = ladder.state.rankers.slice(0, 100);
    await nextTick();
    expect(wrapper.classes()).toContain("overflow-y-auto");
  });

  test("centers by displayed index rather than simulation rank", async () => {
    ladder.getters.shownRankers = ladder.state.rankers.filter(
      (ranker) => ranker.accountId % 10 === 0,
    );
    options.state.ladder.followOwnRanker.value = true;
    mountTable();
    await nextTick();
    expect(wrapper.element.scrollTop).toBe(814);
    expect(mountedIds()).toContain(500);
    expect(mountedIds()[0]).toBe(10);
    expect(ladder.state.rankers).toHaveLength(1000);
  });

  test("handles filters shrinking a scrolled ladder and an empty ladder", async () => {
    mountTable();
    await scrollTo(12000);
    ladder.getters.shownRankers = ladder.state.rankers.slice(0, 3);
    await nextTick();
    expect(mountedIds()).toContain(1);
    expect(mountedIds()).toContain(3);
    ladder.getters.shownRankers = [];
    await nextTick();
    expect(mountedIds()).toEqual([]);
    expect(wrapper.html()).not.toContain("NaN");
    ladder.getters.shownRankers = ladder.state.rankers.slice(0, 1);
    await nextTick();
    expect(mountedIds()).toEqual([1]);
  });

  test("scrolls back to the top when the followed player becomes first", async () => {
    options.state.ladder.followOwnRanker.value = true;
    mountTable();
    await nextTick();
    ladder.getters.yourRanker = ladder.state.rankers[0];
    await nextTick();
    expect(wrapper.element.scrollTop).toBe(0);
  });
});
