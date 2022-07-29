export default {
  namespaced: true,
  state: () => {
    return {
      onTick: [],
    };
  },
  mutations: {
    subscribeToHook(state, { id, fn }) {
      let hooks = Reflect.get(state, id);
      hooks.push(fn);
    },
  },
};
