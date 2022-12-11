import { VueRenderer } from "@tiptap/vue-3";
import tippy from "tippy.js";
import { PluginKey } from "prosemirror-state";
import ChatWindowInputUserMentionList from "~/components/chat/ChatWindowInputSuggestionList.vue";
import emojiJson from "~/assets/emoji.json";

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

const render = (format: Function) => () => {
  let component;
  let popup;
  return {
    onStart: (props) => {
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
    onUpdate(props) {
      component.updateProps(props);
      if (!props.clientRect) {
        return;
      }
      popup[0].setProps({
        getReferenceClientRect: props.clientRect,
      });
    },
    onKeyDown(props) {
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

export const useUserSuggestion = (list: Array<string>) => {
  return {
    items: ({ query }) => {
      const queryLower = query.toLowerCase();
      if (queryLower.length < 1) return [];

      if (queryLower.startsWith("#")) {
        if (queryLower.length < 2) return [];
        return list.filter((_, index) =>
          String(index + 1).includes(queryLower.substring(1))
        );
      } else {
        if (queryLower.length < 1) return [];
        return list.filter((item) => item.toLowerCase().startsWith(queryLower));
      }
    },
    pluginKey: new PluginKey("userSuggestion"),
    render: render((item) => item),
  };
};

export const useEmojiSuggestion = () => {
  return {
    char: ":",
    items: ({ query }) => {
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
    render: render((item) => `${item.emoji}: ${item.aliases[0]}`),
  };
};

export const useGroupSuggestion = (list: Array<string>) => {
  return {
    char: "$",
    items: ({ query }) => {
      const queryLower = query.toLowerCase();
      return [
        ...list.filter((item) => item.toLowerCase().startsWith(queryLower)),
        queryLower,
      ];
    },
    pluginKey: new PluginKey("groupSuggestion"),
    render: render((item) => `$${item}$`),
  };
};
