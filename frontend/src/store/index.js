import { createStore } from "vuex";
import { StompClient } from "@/stomp/stompClient";
import ladderModule from "@/ladder/store/ladderModule";

let store = createStore({
  state: function () {
    return {
      stompClient: new StompClient(),
    };
  },
  getters: {},
  mutations: {},
  actions: {},
  modules: {
    ladder: ladderModule,
  },
});

export default store;
