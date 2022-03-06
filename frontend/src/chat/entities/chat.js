import Message from "@/chat/entities/message";

class Chat {
  constructor(data) {
    this.currentChatNumber = data.currentChatNumber;
    this.messages = [];
    data.messages.forEach((message) => {
      this.messages.push(new Message(message));
    });
  }
}

export default Chat;
