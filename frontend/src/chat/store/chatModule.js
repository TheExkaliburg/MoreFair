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
    updateChat(state, payload) {
      state.chat.update(payload.message);
    },
  },
  actions: {},
  getters: {},
};

export default chatModule;
