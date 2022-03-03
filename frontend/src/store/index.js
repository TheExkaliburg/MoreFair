import { createStore } from "vuex";
import { StompClient } from "@/stomp/stompClient";

let store = createStore({
  state: {
    stompClient: new StompClient(),
  },
  getters: {},
  mutations: {},
  actions: {},
  modules: {},
});

export default store;
