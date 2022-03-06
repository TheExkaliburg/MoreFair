import Ranker from "@/ladder/entities/ranker";

export default {
  namespaced: true,
  state: () => {
    return {
      ladder: [new Ranker({})],
    };
  },
  mutations: {
    connect(state, payload) {
      let stompClient = payload;
      console.log(stompClient);
    },
  },
  actions: {},
  getters: {},
};
