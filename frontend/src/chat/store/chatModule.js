import Chat from "@/chat/entities/chat";

const chatModule = {
  namespaced: true,
  state: () => {
    return {
      chat: new Chat({ currentChatNumber: 1, messages: [] }),
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
          "You have been disconnected from the server, this could be because of a restart or an update. Please try reconnecting in a few minutes or try Discord if you cannot connect at all anymore.",
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
