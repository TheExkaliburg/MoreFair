import { VueRenderer } from "@tiptap/vue-3";
import { tippy } from "vue-tippy";
import { PluginKey } from "prosemirror-state";
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

const render = (format: Function) => () => {
  let component: any;
  let popup: any;
  return {
    onStart: (props: any) => {
      component = new VueRenderer(ChatWindowInputUserMentionList, {
        props: { ...props, format },
        editor: props.editor,
      });
      if (!props.clientRect) {
        return;
      }

      popup = tippy("body", {
        getReferenceClientRect: props.clientRect,
        appendTo: () => document.body,
        content: component.element,
        showOnCreate: true,
        interactive: true,
        trigger: "manual",
        placement: "top-start",
        arrow: false,
        onShow(instance) {
          const tippyBox = instance.popper.querySelector(".tippy-box");
          if (tippyBox === null) return;
          tippyBox.classList.add("tippy-suggestion");
        },
      });
    },
    onUpdate(props: any) {
      component.updateProps(props);
      if (!props.clientRect) {
        return;
      }
      popup[0].setProps({
        getReferenceClientRect: props.clientRect,
      });
    },
    onKeyDown(props: any) {
      if (props.event.key === "Escape") {
        popup[0].hide();
        return true;
      }

      return component.ref?.onKeyDown(props.event);
    },
    onExit() {
      popup[0].destroy();
      component.destroy();
    },
  };
};

export const useUserSuggestion = () => {
  return {
    items: ({ query }: { query: string }) => {
      const list = useChatStore().state.suggestions;
      const queryLower = query.toLowerCase();
      if (queryLower.length < 1) return list;

      if (queryLower.startsWith("#")) {
        if (queryLower.length < 2) return [];
        return list.filter((item) =>
          String(item.accountId).includes(queryLower.substring(1)),
        );
      } else {
        if (queryLower.length < 1) return [];
        return list.filter(
          (item) =>
            item.displayName.toLowerCase().startsWith(queryLower) &&
            item.displayName !== "Mystery Guest",
        );
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
      const list = optionsStore.state.chat.subscribedMentions.get();
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
