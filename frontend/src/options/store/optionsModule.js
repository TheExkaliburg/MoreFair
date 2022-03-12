import { BoolOption, RangeOption } from "../entities/option";

const optionsModule = {
  namespaced: true,
  state: () => {
    return {
      options: [
        new BoolOption({ name: "show_chat", value: true }),
        new BoolOption({ name: "dispense_cookies", value: false }),
        new RangeOption({ name: "cookie_count", value: 69, min: 0, max: 100 }),
      ],
    };
  },
  mutations: {
    init(state) {
      state.options = [];
      //TODO: load locally
      //TODO: load from server
    },
    updateOption(state, { option, value }) {
      const index = state.options.findIndex((o) => o.name === option.name);
      if (index >= 0) {
        state.options[index].value = value;
      } else {
        throw new Error(`Option ${option.name} not found`);
      }
      //TODO: save locally
      //TODO: save to server
    },
  },
  actions: {},
  getters: {},
};

export default optionsModule;
