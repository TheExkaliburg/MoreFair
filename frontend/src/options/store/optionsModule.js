import {
  BoolOption,
  IntegerOption,
  OptionSection,
  RangeOption,
} from "../entities/option";

//import { createHookEndpoint } from "@/store/hooks";

//const optionChangedHook = createHookEndpoint(
//  "optionChanged",
//  (hook, ...[option, allOptions]) => {
//    hook.callback(option, allOptions);
//  }
//);

const optionsModule = {
  namespaced: true,
  store: {},
  setStore(store) {
    this.store = store;
  },
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
            }).setActiveFn(() => {
              return !optionsModule.store.getters["options/getOptionValue"](
                "showAllRankers"
              );
            }),
            new IntegerOption({
              displayName: "Rankers padding",
              name: "rankersPadding",
              value: 100,
            }).setActiveFn(() => {
              return !optionsModule.store.getters["options/getOptionValue"](
                "showAllRankers"
              );
            }),
          ],
        }),
        new OptionSection({
          displayName: "Chat Settings",
          name: "chatSettings",
          options: [
            new BoolOption({
              displayName: "Highlight Mentions",
              name: "highlightMentions",
              value: true,
            }),
            new BoolOption({
              displayName: "Play sound on mention",
              name: "mentionSound",
              value: false,
            }).setActiveFn(() => {
              return optionsModule.store.getters["options/getOptionValue"](
                "highlightMentions"
              );
            }),
            new RangeOption({
              displayName: "Sound Volume",
              name: "mentionSoundVolume",
              value: 100,
              min: 0,
              max: 100,
            }).setActiveFn(() => {
              return (
                optionsModule.store.getters["options/getOptionValue"](
                  "mentionSound"
                ) &&
                optionsModule.store.getters["options/getOptionValue"](
                  "highlightMentions"
                )
              );
            }),
          ],
        }),
        new OptionSection({
          displayName: "Mod Features",
          name: "modFeatures",
          options: [
            new BoolOption({
              displayName: "Enable Moderation Page",
              name: "enableModPage",
              value: false,
            }).setVisibleFn(
              () =>
                !optionsModule.store.getters["options/getOption"]("modFeatures")
                  .visible
            ),
            new BoolOption({
              displayName: "Enable Chat Features",
              name: "enableChatModFeatures",
              value: false,
            }).setVisibleFn(
              () =>
                !optionsModule.store.getters["options/getOption"]("modFeatures")
                  .visible
            ),
          ],
        }),
      ],
    };
  },
  mutations: {
    init(state) {
      state.options = [];
      //TODO: load from server
    },
    loadOptions(state) {
      //TODO: load locally
      try {
        const savedOptions = JSON.parse(localStorage.getItem("options"));
        if (savedOptions) {
          //get all options
          let allOptions = state.options.map(
            (section) => section.options || [section]
          );
          allOptions = [].concat(...allOptions);
          savedOptions.forEach(({ name, value }) => {
            const option = allOptions.find((o) => o.name === name);
            if (option) {
              option.value = value;
            }
          });
          allOptions.forEach((option) => option.updateDisplayProps());
        }
      } catch (e) {
        console.log(state.value);
      }
    },
    updateOption(state, { option, payload }) {
      option.set(payload);
      //This is always guaranteed to be the new value
      //payload may also include other properties that are describing other aspects of the option
      //eslint-disable-next-line no-unused-vars
      const newValue = payload.value;

      //Saving to localstorage
      let allOptions = state.options.map(
        (section) => section.options || [section]
      );
      allOptions = [].concat(...allOptions);
      let optionNamesAndValues = allOptions.map((option) => {
        return {
          name: option.name,
          value: option.value,
        };
      });
      localStorage.setItem("options", JSON.stringify(optionNamesAndValues));

      //Now updating the option's display properties
      allOptions.forEach((option) => option.updateDisplayProps());

      //TODO: save to server

      //Call hooks to let users know that the option has changed
      //optionChangedHook(option, state.options);
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
