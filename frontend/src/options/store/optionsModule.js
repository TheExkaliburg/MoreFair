import {
  BoolOption,
  RangeOption,
  IntegerOption,
  OptionSection,
} from "../entities/option";

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
    },
  },
  actions: {},
  getters: {},
};

export default optionsModule;
