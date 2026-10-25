// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { mount, type VueWrapper } from "@vue/test-utils";
import { nextTick, reactive } from "vue";
import { EditorContent } from "@tiptap/vue-3";
import type { JSONContent } from "@tiptap/core";
import ChatWindowInput from "~/components/chat/ChatWindowInput.vue";
import { Message, MessagePartType } from "~/store/entities/message";

vi.mock("~/store/chat", () => ({
  ChatType: { GLOBAL: "GLOBAL", LADDER: "LADDER", MOD: "MOD" },
  useChatStore: () => chat,
}));
vi.mock("~/store/account", () => ({
  useAccountStore: () => ({ getters: { isMod: false } }),
}));
vi.mock("~/store/options", () => ({
  useOptionsStore: () => ({
    state: { chat: { subscribedMentions: { get: () => ["example"] } } },
  }),
}));
vi.mock("~/composables/useLang", () => ({
  useLang: () => (key: string) => key,
}));

const empty: JSONContent = {
  type: "doc",
  content: [{ type: "paragraph" }],
};
const chat = {
  state: reactive({
    input: empty,
    selectedChatType: "GLOBAL",
    suggestions: [{ displayName: "Example", accountId: 123 }],
  }),
  actions: { sendMessage: vi.fn() },
};
let wrapper: VueWrapper;

beforeEach(async () => {
  chat.state.input = empty;
  chat.state.selectedChatType = "GLOBAL";
  chat.actions.sendMessage.mockClear();
  wrapper = mount(ChatWindowInput, {
    attachTo: document.body,
    global: { stubs: { ChatWindowChannelSelector: true } },
  });
  await nextTick();
});

afterEach(() => wrapper?.unmount());

function editor() {
  const value = wrapper.getComponent(EditorContent).props("editor");
  if (!value) throw new Error("Chat editor was not mounted");
  return value;
}

function doc(content: JSONContent[]): JSONContent {
  return { type: "doc", content: [{ type: "paragraph", content }] };
}

describe("legacy chat editor compatibility", () => {
  test("preserves old mention payloads, labels, and exact send metadata", async () => {
    editor().commands.setContent(
      doc([
        { type: "text", text: "😀 " },
        {
          type: "userMention",
          attrs: { id: { displayName: "Example", accountId: 123 } },
        },
        { type: "text", text: " " },
        { type: "emojiMention", attrs: { id: { emoji: "🙂" } } },
        { type: "text", text: " " },
        { type: "groupMention", attrs: { id: "example" } },
      ]),
    );
    await nextTick();
    expect(editor().getText()).toBe("😀 @Example#123 🙂 $example$");
    expect(wrapper.findAll(".mention").map((node) => node.text())).toEqual([
      "@Example#123",
      "🙂",
      "$example$",
    ]);
    // Legacy CharacterCount counts each atom as one, not its rendered label.
    expect(editor().storage.characterCount.characters()).toBe(8);
    await wrapper.get("button").trigger("click");
    expect(chat.actions.sendMessage).toHaveBeenCalledExactlyOnceWith(
      "😀 {@} 🙂 {$}",
      [
        { u: "Example", i: 3, id: 123 },
        { g: "example", i: 10 },
      ],
    );
    expect(editor().isEmpty).toBe(true);
    expect(chat.state.input.content?.[0]?.content).toBeUndefined();
  });

  test("limits plain text to 280 UTF-16 units", async () => {
    editor().commands.setContent(
      doc([{ type: "text", text: "a".repeat(279) }]),
    );
    editor().commands.insertContentAt(280, "b");
    expect(editor().storage.characterCount.characters()).toBe(280);
    editor().commands.insertContentAt(281, "c");
    expect(editor().getText()).toBe("a".repeat(279) + "b");
    await nextTick();
  });

  test("synchronizes external content and clears slash channel commands", async () => {
    chat.state.input = doc([{ type: "text", text: "hello" }]);
    await nextTick();
    expect(editor().getText()).toBe("hello");
    editor().commands.setContent(doc([{ type: "text", text: "/local " }]));
    chat.state.input = editor().getJSON();
    await nextTick();
    expect(chat.state.selectedChatType).toBe("LADDER");
    expect(editor().isEmpty).toBe(true);
    expect(chat.actions.sendMessage).not.toHaveBeenCalled();
  });

  test.each([
    ["userMention", { displayName: "Example", accountId: 123 }, "@"],
    ["emojiMention", { emoji: "🙂" }, ":"],
    ["groupMention", "example", "$"],
  ])(
    "restores the correct Backspace trigger for old %s JSON",
    async (type, id, trigger) => {
      editor().commands.setContent(doc([{ type, attrs: { id } }]));
      await nextTick();
      const json: JSONContent = editor().getJSON();
      expect(json.content?.[0]?.content?.[0]?.attrs?.id).toEqual(id);
      editor().commands.setTextSelection(2);
      editor().commands.keyboardShortcut("Backspace");
      await nextTick();
      expect(editor().getText()).toBe(trigger);
    },
  );

  test("counts surrogate pairs and atoms without changing the legacy limit", async () => {
    editor().commands.setContent(
      doc([
        { type: "text", text: "😀".repeat(139) },
        { type: "groupMention", attrs: { id: "a-very-long-group-label" } },
        { type: "text", text: "x" },
      ]),
    );
    expect(editor().storage.characterCount.characters()).toBe(280);
    editor().commands.insertContentAt(editor().state.doc.content.size - 1, "y");
    expect(editor().storage.characterCount.characters()).toBe(280);
    expect(editor().getText()).toBe(
      "😀".repeat(139) + "$a-very-long-group-label$x",
    );
    await nextTick();
  });

  test("truncates over-limit paste and allows selection replacement", async () => {
    editor().view.dispatch(
      editor().state.tr.insertText("a".repeat(281), 1).setMeta("paste", true),
    );
    expect(editor().getText()).toBe("a".repeat(280));
    editor().commands.setTextSelection({ from: 1, to: 281 });
    editor().commands.insertContent("😀");
    expect(editor().getText()).toBe("😀");
    expect(editor().storage.characterCount.characters()).toBe(2);
    expect(editor().state.doc.childCount).toBe(1);
    await nextTick();
  });

  test("does not feed editor updates back into the store or lose selection", async () => {
    const update = vi.fn();
    editor().on("update", update);
    editor().commands.insertContent("hello");
    await nextTick();
    expect(update).toHaveBeenCalledOnce();
    expect(chat.state.input).toEqual(editor().getJSON());
    expect(editor().state.selection.from).toBe(6);
    chat.state.input = doc([{ type: "text", text: "hi" }]);
    await nextTick();
    expect(editor().getText()).toBe("hi");
    expect(editor().state.selection.from).toBe(3);
    expect(update).toHaveBeenCalledOnce();
    editor().off("update", update);
  });

  test("only sends once for Enter and ignores IME composition", async () => {
    editor().commands.insertContent("hello");
    await nextTick();
    const composing = new KeyboardEvent("keydown", {
      key: "Enter",
      bubbles: true,
      cancelable: true,
      isComposing: true,
    });
    wrapper.get(".ProseMirror").element.dispatchEvent(composing);
    await nextTick();
    expect(chat.actions.sendMessage).not.toHaveBeenCalled();
    await wrapper.get(".ProseMirror").trigger("keydown", { key: "Enter" });
    expect(chat.actions.sendMessage).toHaveBeenCalledExactlyOnceWith(
      "hello",
      [],
    );
    await nextTick();
    expect(editor().isEmpty).toBe(true);
  });

  test("renders existing saved messages using unchanged UTF-16 metadata offsets", () => {
    const message = new Message({
      message: "😀 {@} 🙂 {$}",
      metadata: JSON.stringify([
        { u: "Example", i: 3, id: 123 },
        { g: "example", i: 10 },
      ]),
    });
    expect(message.getMessageParts()).toEqual([
      { type: MessagePartType.plain, text: "😀 " },
      { type: MessagePartType.mentionUser, text: "Example" },
      { type: MessagePartType.mentionUserId, text: "123" },
      { type: MessagePartType.plain, text: " 🙂 " },
      { type: MessagePartType.mentionGroup, text: "example" },
      { type: MessagePartType.plain, text: "" },
    ]);
  });
});
