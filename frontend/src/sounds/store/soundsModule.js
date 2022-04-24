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
        console.warn(`Sound ${name} already registered`);
        return true;
      } else {
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
