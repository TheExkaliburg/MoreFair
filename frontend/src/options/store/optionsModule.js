import {
  BoolOption,
  RangeOption,
  IntegerOption,
  OptionSection,
} from "../entities/option";

import { createHookEndpoint } from "@/modules/hooks";

const optionChangedHook = createHookEndpoint(
  "optionChanged",
  (hook, ...[option, allOptions]) => {
    hook.callback(option, allOptions);
  }
);

const optionsModule = {
  namespaced: true,
  state: () => {
    return {
      options: [
        new OptionSection({
          displayName: "General",
          name: "general",
          options: [
            new BoolOption({
              displayName: "Show all rankers",
              name: "showAllRankers",
              value: false,
            }),
            new RangeOption({
              displayName: "Rankers at top",
              name: "rankersAtTop",
              value: 10,
              min: 0,
              max: 100,
            }),
            new IntegerOption({
              displayName: "Rankers padding",
              name: "rankersPadding",
              value: 1000,
            }),
          ],
        }),
        new OptionSection({
          displayName: "Lynn's Chad tweaks",
          name: "lynnsChadTweaks",
          options: [
            new BoolOption({
              displayName: "Un-Invert Chad",
              name: "invertChad",
              value: false,
            }),
            new BoolOption({
              displayName: "Un-Invert Lynn",
              name: "invertLynn",
              value: false,
            }),
            new BoolOption({
              displayName: "Highlight Mentions",
              name: "highlightMentions",
              value: true,
            }),
          ],
        }),
      ],
    };
  },
  mutations: {
    init(state) {
      state.options = [];
      //TODO: load locally
      //TODO: load from server
    },
    updateOption(state, { option, payload }) {
      option.set(payload);
      //This is always guaranteed to be the new value
      //payload may also include other properties that are describing other aspects of the option
      //eslint-disable-next-line no-unused-vars
      const newValue = payload.value;
      //TODO: save locally
      //TODO: save to server

      //Call hooks to let users know that the option has changed
      optionChangedHook(option, state.options);
    },
  },
  actions: {},
  getters: {
    getOption: (state) => (name) => {
      for (const section of state.options) {
        if (section.name === name) {
          return section;
        }
        if (!(section instanceof OptionSection)) {
          continue;
        }
        for (const option of section.options) {
          if (option.name === name) {
            return option;
          }
        }
      }
    },
    getOptionValue: (state, getters) => (name) => {
      const option = getters.getOption(name);
      if (option) {
        return option.get();
      }
      return null;
    },
  },
};

export default optionsModule;
