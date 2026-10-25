import { VueRenderer } from "@tiptap/vue-3";
import { tippy } from "vue-tippy";
import { PluginKey } from "@tiptap/pm/state";
import type {
  SuggestionKeyDownProps,
  SuggestionProps,
} from "@tiptap/suggestion";
import ChatWindowInputUserMentionList from "../components/chat/ChatWindowInputSuggestionList.vue";
import { useOptionsStore } from "~/store/options";
import { Suggestion, useChatStore } from "~/store/chat";

const emojiJson = () => import("../assets/emoji.json");

export type EmojiDataEntry = {
  emoji: string;
  description: string;
  category: string;
  aliases: string[];
  tags: string[];
  unicode_version: string;
  ios_version: string;
};

const render =
  <T>(format: (item: T) => string) =>
  () => {
    let component: VueRenderer | undefined;
    let popup: ReturnType<typeof tippy>[number] | undefined;

    const destroy = () => {
      popup?.destroy();
      popup = undefined;
      component?.destroy();
      component = undefined;
    };
    const updatePopup = (props: SuggestionProps<T, { id: T }>) => {
      const rect = props.clientRect?.();
      if (!rect || !component?.element) {
        popup?.hide();
        return;
      }
      const getReferenceClientRect = () => props.clientRect?.() ?? rect;
      if (popup) {
        popup.setProps({ getReferenceClientRect });
        popup.show();
        return;
      }
      [popup] = tippy("body", {
        getReferenceClientRect,
        appendTo: () => document.body,
        content: component.element,
        showOnCreate: true,
        interactive: true,
        trigger: "manual",
        placement: "top-start",
        arrow: false,
        onShow(instance) {
          instance.popper
            .querySelector(".tippy-box")
            ?.classList.add("tippy-suggestion");
        },
      });
    };

    return {
      onStart: (props: SuggestionProps<T, { id: T }>) => {
        destroy();
        if (props.editor.isDestroyed) return;
        component = new VueRenderer(ChatWindowInputUserMentionList, {
          props: { ...props, format },
          editor: props.editor,
        });
        updatePopup(props);
      },
      onUpdate(props: SuggestionProps<T, { id: T }>) {
        if (!component) return;
        component.updateProps(props);
        updatePopup(props);
      },
      onKeyDown(props: SuggestionKeyDownProps): boolean {
        if (props.event.key === "Escape") {
          const wasOpen = popup !== undefined;
          popup?.hide();
          return wasOpen;
        }
        const list:
          { onKeyDown?: (event: KeyboardEvent) => boolean } | undefined =
          component?.ref;
        const handled = list?.onKeyDown?.(props.event) ?? false;
        // Accepting a suggestion must not bubble into the chat's Enter-to-send.
        if (handled) props.event.stopPropagation();
        return handled;
      },
      onExit: destroy,
    };
  };

export const useUserSuggestion = () => {
  return {
    items: ({ query }: { query: string }) => {
      const list = useChatStore().state.suggestions;
      const queryLower = query.toLowerCase();

      if (queryLower.startsWith("#")) {
        if (queryLower.length < 2) return list.slice(0, 10);
        return list
          .filter((item) =>
            String(item.accountId).startsWith(queryLower.substring(1)),
          )
          .slice(0, 10);
      } else {
        const filteredList = list.filter(
          (item) => item.displayName !== "Mystery Guest",
        );
        if (queryLower.length < 1) return filteredList.slice(0, 10);
        return filteredList
          .filter((item) => item.displayName.toLowerCase().includes(queryLower))
          .slice(0, 10);
      }
    },
    pluginKey: new PluginKey("userSuggestion"),
    render: render(
      (item: Suggestion) => `${item.displayName}#${item.accountId}`,
    ),
  };
};

export const useEmojiSuggestion = () => {
  return {
    char: ":",
    items: async ({ query }: { query: string }) => {
      const emojiData: EmojiDataEntry[] = (await emojiJson()).default;
      const queryLower = query.toLowerCase();
      if (queryLower.length < 2) return [];
      return emojiData.filter((item) => {
        return (
          item.aliases.some((alias) => alias.startsWith(queryLower)) ||
          item.tags.some((tag) => tag.startsWith(queryLower))
        );
      });
    },
    pluginKey: new PluginKey("emojiSuggestion"),
    render: render(
      (item: EmojiDataEntry) => `${item.emoji}: ${item.aliases[0]}`,
    ),
  };
};

export const useGroupSuggestion = () => {
  const optionsStore = useOptionsStore();
  return {
    char: "$",
    items: ({ query }: { query: string }) => {
      const list = [...optionsStore.state.chat.subscribedMentions.get()];
      if (!list.includes("mods")) list.push("mods");
      const queryLower = query.toLowerCase();
      return [
        ...list.filter((item: string) =>
          item.toLowerCase().startsWith(queryLower),
        ),
        queryLower,
      ];
    },
    pluginKey: new PluginKey("groupSuggestion"),
    render: render((item: string) => `$${item}$`),
  };
};
