import Message from "@/chat/entities/message";

class Chat {
  constructor(data) {
    this.currentChatNumber = data.currentChatNumber;
    this.messages = [];
    data.messages.forEach((message) => {
      this.messages.unshift(new Message(message));
    });
  }

  addNewMessage(message) {
    if (!(message instanceof Message)) message = new Message(message);
    this.messages.push(message);
    if (this.messages.length > 50) {
      this.messages.shift();
    }
  }

  update(message) {
    if (message) {
      message.forEach((event) => {
        switch (event.eventType) {
          case "NAME_CHANGE":
            this.handleNameChange(event);
            break;
          case "BAN":
          case "MUTE":
            this.removeMessages(event);
            break;
        }
      });
    }
  }

  msgFlag({ message, flag, type }) {
    if (type === "set") {
      message.setFlag(flag);
    } else if (type === "unset") {
      message.unsetFlag(flag);
    }
  }

  removeMessages(event) {
    this.messages = this.messages.filter(
      (m) => event.accountId !== m.accountId
    );
  }

  handleNameChange(event) {
    this.messages.forEach((message) => {
      if (event.accountId === message.accountId) {
        message.username = event.data;
      }
    });
  }
}

export default Chat;
