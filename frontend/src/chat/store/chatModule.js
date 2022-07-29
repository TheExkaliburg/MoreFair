import Chat from "@/chat/entities/chat";

const chatModule = {
  namespaced: true,
  state: () => {
    return {
      chat: {},
    };
  },
  mutations: {
    init(state, payload) {
      state.chat = new Chat(payload.message.content);
    },
    addMessage(state, payload) {
      state.chat.addNewMessage(payload.message);
    },
    handleNameChangeEvent(state, { event }) {
      state.chat.handleNameChange(event);
    },
    update(state, payload) {
      state.chat.update(payload.message);
    },
    msgFlag(state, payload) {
      state.chat.msgFlag(payload);
    },
    addRestartMessage(state) {
      state.chat.addNewMessage({
        username: "Chad",
        message:
          "The server is restarting, please stay patient and try again in a few minutes.",
        tag: "🂮",
        ahPoints: 5950,
        accountId: 1,
        timeCreated: "Now",
        metadata: "[]",
      });
    },
  },
  actions: {},
  getters: {},
};

export default chatModule;
