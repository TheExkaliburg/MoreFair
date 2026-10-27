// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { Editor, Node } from "@tiptap/core";
import { Paragraph } from "@tiptap/extension-paragraph";
import { Text } from "@tiptap/extension-text";
import { Suggestion, type SuggestionProps } from "@tiptap/suggestion";
import { flushPromises } from "@vue/test-utils";
import {
  useEmojiSuggestion,
  useGroupSuggestion,
  useUserSuggestion,
  type EmojiDataEntry,
} from "~/composables/useSuggestion";

const mocks = vi.hoisted(() => ({
  keydown: vi.fn(() => true),
  update: vi.fn(),
  destroy: vi.fn(),
  hide: vi.fn(),
  show: vi.fn(),
  popupDestroy: vi.fn(),
  setProps: vi.fn(),
  createPopup: vi.fn(),
}));

vi.mock("@tiptap/vue-3", () => ({
  VueRenderer: class {
    element = document.createElement("div");
    ref = { onKeyDown: mocks.keydown };
    updateProps = mocks.update;
    destroy = mocks.destroy;
  },
}));
vi.mock("vue-tippy", () => ({
  tippy: (...args: unknown[]) => {
    mocks.createPopup(...args);
    return [
      {
        hide: mocks.hide,
        show: mocks.show,
        destroy: mocks.popupDestroy,
        setProps: mocks.setProps,
      },
    ];
  },
}));
vi.mock("~/store/chat", () => ({
  useChatStore: () => ({
    state: {
      suggestions: [
        { accountId: 42, displayName: "Mystery Guest" },
        { accountId: 123, displayName: "Example" },
      ],
    },
  }),
}));
vi.mock("~/store/options", () => ({
  useOptionsStore: () => ({
    state: { chat: { subscribedMentions: { get: () => ["example"] } } },
  }),
}));

let editor: Editor;

beforeEach(() => {
  vi.clearAllMocks();
  editor = new Editor({
    extensions: [
      Node.create({ name: "doc", topNode: true, content: "block" }),
      Paragraph,
      Text,
    ],
  });
});

afterEach(() => editor.destroy());

function keydown(key: string) {
  return {
    view: editor.view,
    event: new KeyboardEvent("keydown", { key }),
    range: { from: 1, to: 3 },
  };
}

function props(
  clientRect: (() => DOMRect | null) | null = () => new DOMRect(1, 2, 3, 4),
): SuggestionProps<EmojiDataEntry, { id: EmojiDataEntry }> {
  return {
    editor,
    range: { from: 1, to: 3 },
    query: "s",
    text: ":s",
    items: [],
    command: vi.fn(),
    decorationNode: null,
    clientRect,
    loading: false,
    placement: "top-start",
    offset: { mainAxis: 4, crossAxis: 0 },
    flip: true,
    floatingUi: {
      placement: "top-start",
      strategy: "absolute",
      middleware: [],
    },
    mount: () => () => {},
  };
}

describe("chat suggestion compatibility", () => {
  test("keeps Mystery Guest filtering except for account-ID searches", () => {
    const suggestion = useUserSuggestion();
    expect(suggestion.items({ query: "" })).toEqual([
      { accountId: 123, displayName: "Example" },
    ]);
    expect(suggestion.items({ query: "EXA" })).toHaveLength(1);
    expect(suggestion.items({ query: "#4" })).toEqual([
      { accountId: 42, displayName: "Mystery Guest" },
    ]);
  });

  test("preserves emoji thresholds and group suggestions", async () => {
    const emoji = useEmojiSuggestion();
    expect(await emoji.items({ query: "s" })).toEqual([]);
    expect(await emoji.items({ query: "smi" })).not.toHaveLength(0);
    const group = useGroupSuggestion();
    expect(group.items({ query: "mo" })).toEqual(["mods", "mo"]);
    expect(group.items({ query: "EX" })).toEqual(["example", "ex"]);
    expect(
      new Set([useUserSuggestion().pluginKey, emoji.pluginKey, group.pluginKey])
        .size,
    ).toBe(3);
  });
});

describe("emoji popup lifecycle regression", () => {
  test("does not dereference an uncreated component while emoji results are pending", () => {
    const renderer = useEmojiSuggestion().render();
    // The beta plugin can deliver keydown before awaiting items() and onStart.
    expect(renderer.onKeyDown(keydown("ArrowDown"))).toBe(false);
    expect(renderer.onKeyDown(keydown("Enter"))).toBe(false);
    expect(renderer.onKeyDown(keydown("Escape"))).toBe(false);
    expect(() => renderer.onExit()).not.toThrow();
    expect(mocks.keydown).not.toHaveBeenCalled();
  });

  test("creates a popup when missing initial geometry becomes available", () => {
    const renderer = useEmojiSuggestion().render();
    renderer.onStart(props(null));
    expect(mocks.createPopup).not.toHaveBeenCalled();
    renderer.onKeyDown(keydown("Escape"));
    renderer.onUpdate(props());
    expect(mocks.createPopup).toHaveBeenCalledOnce();
    const selection = keydown("Enter");
    expect(renderer.onKeyDown(selection)).toBe(true);
    expect(selection.event.cancelBubble).toBe(true);
    renderer.onUpdate(props(() => null));
    expect(mocks.hide).toHaveBeenCalledOnce();
    renderer.onUpdate(props());
    expect(mocks.show).toHaveBeenCalledOnce();
    renderer.onExit();
    renderer.onExit();
    renderer.onUpdate(props());
    expect(renderer.onKeyDown(keydown("Enter"))).toBe(false);
    expect(mocks.destroy).toHaveBeenCalledOnce();
    expect(mocks.popupDestroy).toHaveBeenCalledOnce();
    expect(mocks.update).toHaveBeenCalledTimes(3);
  });

  test("supports teardown without geometry and ignores a destroyed editor", () => {
    const renderer = useEmojiSuggestion().render();
    renderer.onStart(props(null));
    renderer.onExit();
    expect(mocks.destroy).toHaveBeenCalledOnce();
    expect(mocks.popupDestroy).not.toHaveBeenCalled();
    editor.destroy();
    renderer.onStart(props());
    expect(mocks.createPopup).not.toHaveBeenCalled();
    expect(renderer.onKeyDown(keydown("Enter"))).toBe(false);
  });

  test.each(["Escape", "unmount"])(
    "does not revive a dismissed popup after pending emoji results (%s)",
    async (action) => {
      const pending = Promise.withResolvers<EmojiDataEntry[]>();
      editor.registerPlugin(
        Suggestion({
          ...useEmojiSuggestion(),
          editor,
          items: () => pending.promise,
        }),
      );
      editor.commands.insertContent(":s");
      expect(() => {
        editor.view.someProp("handleKeyDown", (handler) =>
          handler(editor.view, keydown("ArrowDown").event),
        );
      }).not.toThrow();
      if (action === "Escape") {
        editor.view.someProp("handleKeyDown", (handler) =>
          handler(editor.view, keydown("Escape").event),
        );
      } else {
        editor.destroy();
      }
      const updates = mocks.update.mock.calls.length;
      pending.resolve([]);
      await flushPromises();
      expect(mocks.update).toHaveBeenCalledTimes(updates);
      expect(mocks.destroy).toHaveBeenCalledOnce();
      expect(mocks.popupDestroy).toHaveBeenCalledOnce();
    },
  );

  test("ignores stale emoji results after the query changes", async () => {
    const first = Promise.withResolvers<EmojiDataEntry[]>();
    const second = Promise.withResolvers<EmojiDataEntry[]>();
    editor.registerPlugin(
      Suggestion({
        ...useEmojiSuggestion(),
        editor,
        items: ({ query }) => (query === "s" ? first.promise : second.promise),
      }),
    );
    editor.commands.insertContent(":s");
    editor.commands.insertContent("m");
    second.resolve([]);
    await flushPromises();
    const updates = mocks.update.mock.calls.length;
    first.resolve([]);
    await flushPromises();
    expect(mocks.update).toHaveBeenCalledTimes(updates);
  });
});
