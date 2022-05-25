const optionsModule = {
  namespaced: true,
  state: () => {
    return {
      sounds: {},
    };
  },
  mutations: {
    init(state) {
      state.sounds = {};
    },
    loadSound(state, { name, url }) {
      if (!state.sounds[name]) {
        state.sounds[name] = new Audio(url);
        return true;
      } else {
        console.warn(`Sound ${name} already registered`);
        return false;
      }
    },
  },
  actions: {},
  getters: {
    getSound: (state) => (name) => {
      return state.sounds[name];
    },
  },
};

export default optionsModule;
