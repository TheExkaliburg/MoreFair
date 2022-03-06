import { createStore } from "vuex";
import ladderModule from "@/ladder/store/ladderModule";
import chatModule from "@/chat/store/chatModule";
import Settings from "@/store/entities/settings";
import UserDetails from "@/store/entities/userDetails";
import { numberformat } from "swarm-numberformat";

let store = createStore({
  strict: process.env.NODE_ENV !== "production",
  state: () => {
    return {
      settings: undefined,
      user: undefined,
      numberFormatter: new numberformat.Formatter({
        format: "hybrid",
        sigfigs: 6,
        flavor: "short",
        minSuffix: 1e10,
        maxSmall: 0,
      }),
    };
  },
  getters: {},
  mutations: {
    initSettings(state, payload) {
      if (payload.message.content) {
        state.settings = new Settings(payload.message.content);
      }
    },
    initUser(state, payload) {
      if (
        (payload.message.status === "OK" ||
          payload.message.status === "CREATED") &&
        payload.message.content
      ) {
        state.user = new UserDetails(payload.message.content);
      }
    },
  },
  actions: {},
  modules: {
    ladder: ladderModule,
    chat: chatModule,
  },
});
export default store;
