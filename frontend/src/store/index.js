import { createStore } from "vuex";
import ladderModule from "@/store/modules/ladder/ladderModule";
import chatModule from "@/chat/store/chatModule";
import Settings from "@/store/entities/settings";
import UserDetails from "@/store/entities/userDetails";
import { numberformat } from "swarm-numberformat";
import optionsModule from "@/options/store/optionsModule";
import soundsModule from "@/sounds/store/soundsModule";
import moderationModule from "@/moderation/store/moderationModule";
import versioningModule from "@/versioning/store/versioningModule";

import { computed } from "vue";
import { Sounds } from "@/modules/sounds";
import Cookies from "js-cookie";
import API from "@/websocket/wsApi";

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
    increaseAssholeLadder(state, { event }) {
      if (event.data) state.settings.assholeLadder = event.data;
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
    setupConnection({ dispatch }, { stompClient }) {
      stompClient.connect(() => {
        let uuid = Cookies.get("_uuid");
        if (
          (!uuid || uuid === "") &&
          !confirm("Do you want to create a new account?")
        ) {
          return;
        }
        stompClient.subscribe(
          API.ACCOUNT.QUEUE_LOGIN_DESTINATION,
          (message) => {
            store.commit({ type: "initUser", message: message });
            dispatch({
              type: "setupGame",
              stompClient: stompClient,
            });
            dispatch({
              type: "setupChat",
              stompClient: stompClient,
            });
          }
        );
        stompClient.send(API.ACCOUNT.APP_LOGIN_DESTINATION);
      });
    },
    setupGame({ dispatch }, { stompClient }) {
      let highestLadderReached = store.state.user.highestCurrentLadder;
      stompClient.subscribe(API.FAIR.QUEUE_INFO_DESTINATION, (message) => {
        store.commit({ type: "initSettings", message: message });

        stompClient.subscribe(API.GAME.QUEUE_INIT_DESTINATION, (message) => {
          dispatch({
            type: "ladder/setup",
            message: message,
          });
        });

        stompClient.subscribe(
          API.GAME.TOPIC_EVENTS_DESTINATION(highestLadderReached),
          (message) => {
            dispatch({
              type: "ladder/handleLadderEvent",
              message: message,
              stompClient: stompClient,
            });
          }
        );

        stompClient.subscribe(
          API.GAME.TOPIC_GLOBAL_EVENTS_DESTINATION,
          (message) => {
            dispatch({
              type: "ladder/handleGlobalEvent",
              message: message,
              stompClient: stompClient,
            });
          }
        );

        stompClient.subscribe(
          API.GAME.PRIVATE_EVENTS_DESTINATION(Cookies.get("_uuid")),
          (message) => {
            dispatch({
              type: "ladder/handlePrivateEvent",
              message: message,
              stompClient: stompClient,
            });
          }
        );

        stompClient.subscribe(API.GAME.TOPIC_TICK_DESTINATION, (message) => {
          dispatch({
            type: "ladder/calculate",
            message: message,
          });
        });

        stompClient.send(API.GAME.APP_INIT_DESTINATION(highestLadderReached));
      });
      stompClient.send(API.FAIR.APP_INFO_DESTINATION);
    },
    setupChat({ commit }, { stompClient }) {
      let highestLadderReached = store.state.user.highestCurrentLadder;

      stompClient.subscribe(API.CHAT.QUEUE_INIT_DESTINATION, (message) => {
        commit({ type: "chat/init", message: message });
      });

      stompClient.subscribe(
        API.CHAT.TOPIC_EVENTS_DESTINATION(highestLadderReached),
        (message) => {
          commit({ type: "chat/addMessage", message: message });
        }
      );

      stompClient.send(API.CHAT.APP_INIT_DESTINATION(highestLadderReached));
    },
    incrementHighestLadder({ state, commit, dispatch }, { stompClient }) {
      stompClient.unsubscribe(
        API.GAME.TOPIC_EVENTS_DESTINATION(state.ladder.number)
      );
      stompClient.unsubscribe(
        API.CHAT.TOPIC_EVENTS_DESTINATION(state.chat.chat.currentChatNumber)
      );

      //now doing a jingle for boozle <3
      if (reachingFirstSound.value) {
        Sounds.play("promotionJingle", promotionJingleVolume.value);
      }

      commit({
        type: "setHighestLadder",
        payload: state.user.highestCurrentLadder + 1,
      });

      let newHighestLadder = state.user.highestCurrentLadder;

      stompClient.subscribe(
        API.GAME.TOPIC_EVENTS_DESTINATION(newHighestLadder),
        (message) => {
          dispatch({
            type: "ladder/update",
            message: message,
            stompClient: stompClient,
          });
        }
      );
      stompClient.subscribe(
        API.CHAT.TOPIC_EVENTS_DESTINATION(newHighestLadder),
        (message) => {
          commit({ type: "chat/addMessage", message: message });
        }
      );
      stompClient.send(API.GAME.APP_INIT_DESTINATION(newHighestLadder));
      stompClient.send(API.CHAT.APP_INIT_DESTINATION(newHighestLadder));
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
