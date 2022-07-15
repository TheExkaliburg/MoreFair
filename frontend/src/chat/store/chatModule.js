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
  },
  actions: {},
  getters: {},
};

export default chatModule;
