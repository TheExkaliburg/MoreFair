import { createStore } from "vuex";
import { StompClient } from "@/websocket/stompClient";
import ladderModule from "@/ladder/store/ladderModule";
import chatModule from "@/chat/store/chatModule";
import Info from "@/store/info";

let store = createStore({
  state: () => {
    console.log("Init main Store");
    return {
      stompClient: new StompClient(),
      info: new Info(),
    };
  },
  getters: {},
  mutations: {
    connect(state, payload) {
      state.stompClient.connect();
      console.log(payload);
    },
  },
  actions: {},
  modules: {
    ladder: ladderModule,
    chat: chatModule,
  },
});
export default store;
