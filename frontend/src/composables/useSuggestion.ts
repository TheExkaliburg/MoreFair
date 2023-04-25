import { VueRenderer } from "@tiptap/vue-3";
import tippy from "tippy.js";
import { PluginKey } from "prosemirror-state";
import ChatWindowInputUserMentionList from "../components/chat/ChatWindowInputSuggestionList.vue";

import emojiJson from "../assets/emoji.json";
import { useLadderStore } from "~/store/ladder";
import { useOptionsStore } from "~/store/options";
import { Ranker } from "~/store/entities/ranker";

export type EmojiDataEntry = {
  emoji: string;
  description: string;
  category: string;
  aliases: string[];
  tags: string[];
  unicode_version: string;
  ios_version: string;
};

const emojiData: EmojiDataEntry[] = emojiJson;
const ladderStore = useLadderStore();
const optionsStore = useOptionsStore();

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
        placement: "bottom-start",
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
      const list = ladderStore.state.rankers;
      const queryLower = query.toLowerCase();
      if (queryLower.length < 1) return [];

      if (queryLower.startsWith("#")) {
        if (queryLower.length < 2) return [];
        return list.filter((item) =>
          String(item.accountId).includes(queryLower.substring(1))
        );
      } else {
        if (queryLower.length < 1) return [];
        return list.filter((item) =>
          item.username.toLowerCase().startsWith(queryLower)
        );
      }
    },
    pluginKey: new PluginKey("userSuggestion"),
    render: render((item: Ranker) => `${item.username}#${item.accountId}`),
  };
};

export const useEmojiSuggestion = () => {
  return {
    char: ":",
    items: ({ query }: { query: string }) => {
      const queryLower = query.toLowerCase();
      if (queryLower.length < 3) return [];
      return emojiData.filter((item) => {
        return (
          item.aliases.some((alias) => alias.startsWith(queryLower)) ||
          item.tags.some((tag) => tag.startsWith(queryLower))
        );
      });
    },
    pluginKey: new PluginKey("emojiSuggestion"),
    render: render(
      (item: EmojiDataEntry) => `${item.emoji}: ${item.aliases[0]}`
    ),
  };
};

export const useGroupSuggestion = () => {
  return {
    char: "$",
    items: ({ query }: { query: string }) => {
      const list = optionsStore.state.chat.subscribedMentions.get();
      const queryLower = query.toLowerCase();
      return [
        ...list.filter((item: string) =>
          item.toLowerCase().startsWith(queryLower)
        ),
        queryLower,
      ];
    },
    pluginKey: new PluginKey("groupSuggestion"),
    render: render((item: string) => `$${item}$`),
  };
};
