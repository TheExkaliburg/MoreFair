export default {
  namespaced: true,
  state: () => {
    return {
      onTick: [],
    };
  },
  mutations: {
    addToHook(state, { id, fn }) {
      let hooks = Reflect.get(state, id);
      hooks.push(fn);
    },
  },
};
