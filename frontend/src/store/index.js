import { createStore } from "vuex";
import ladderModule from "@/ladder/store/ladderModule";
import chatModule from "@/chat/store/chatModule";
import Settings from "@/store/entities/settings";
import UserDetails from "@/store/entities/userDetails";
import { numberformat } from "swarm-numberformat";
import optionsModule from "@/options/store/optionsModule";
import soundsModule from "@/sounds/store/soundsModule";
import moderationModule from "@/moderation/store/moderationModule";
import versioningModule from "@/versioning/store/versioningModule";

import { computed } from "vue";

let promotionJingleVolume;
let reachingFirstSound;

let store = createStore({
  strict: process.env.NODE_ENV !== "production",
  namespaced: true,
  state: () => {
    return {
      settings: Settings.placeholder(),
      user: UserDetails.placeholder(),
      numberFormatter: new numberformat.Formatter({
        format: "hybrid",
        sigfigs: 6,
        flavor: "short",
        minSuffix: 1e10,
        maxSmall: 0,
      }),
    };
  },
  getters: {
    isMod(state) {
      return (
        state.user.accessRole === "OWNER" ||
        state.user.accessRole === "MODERATOR"
      );
    },
  },
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
        state.user.saveUUID();
      }
    },
    setHighestLadder(state, { payload }) {
      state.user.highestCurrentLadder = payload;
    },
  },
  actions: {
    incrementHighestLadder({ state, commit, dispatch }, { stompClient }) {
      stompClient.unsubscribe(
        "/topic/ladder/" + state.ladder.ladder.ladderNumber
      );
      stompClient.unsubscribe(
        "/topic/chat/" + state.chat.chat.currentChatNumber
      );

      //now doing a jingle for boozle <3
      if (reachingFirstSound.value) {
        Sounds.play("promotionJingle", promotionJingleVolume.value);
      }

      commit({
        type: "setHighestLadder",
        payload: state.user.highestCurrentLadder + 1,
      });

      stompClient.subscribe(
        "/topic/ladder/" + state.user.highestCurrentLadder,
        (message) => {
          dispatch({
            type: "ladder/update",
            message: message,
            stompClient: stompClient,
          });
        }
      );
      stompClient.subscribe(
        "/topic/chat/" + state.user.highestCurrentLadder,
        (message) => {
          commit({ type: "chat/addMessage", message: message });
        }
      );
      stompClient.send("/app/ladder/init/" + state.user.highestCurrentLadder);
      stompClient.send("/app/chat/init/" + state.user.highestCurrentLadder);
    },
  },
  modules: {
    ladder: ladderModule,
    chat: chatModule,
    //options: optionsModule,
    sounds: soundsModule,
    mod: moderationModule,
    versioning: versioningModule,
  },
});

import { Sounds } from "@/modules/sounds";

promotionJingleVolume = computed(() =>
  store.getters["options/getOptionValue"]("notificationVolume")
);

reachingFirstSound = computed(() =>
  store.getters["options/getOptionValue"]("reachingFirstSound")
);

Sounds.setStore(store);

Sounds.register("promotionJingle", require("@/assets/promotionJingle.wav"));

optionsModule.setStore(store);
store.registerModule("options", optionsModule);
window.store = store;
export default store;
