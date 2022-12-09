import { VueRenderer } from "@tiptap/vue-3";
import tippy from "tippy.js";
import ChatWindowInputMentionList from "~/components/chat/ChatWindowInputMentionList.vue";

export const useSuggestion = () => {
  return {
    items: ({ query }) => {
      return ["Lynn", "Kali", "Grapes", "Clu"]
        .filter((item) => item.toLowerCase().startsWith(query.toLowerCase()))
        .slice(0, 5);
    },
    render: () => {
      let component;
      let popup;
      return {
        onStart: (props) => {
          component = new VueRenderer(ChatWindowInputMentionList, {
            props,
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
    },
  };
};
