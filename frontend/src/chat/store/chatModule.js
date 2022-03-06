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
    updateChat(state, payload) {
      console.log(payload);
    },
  },
  actions: {},
  getters: {},
};

export default chatModule;
