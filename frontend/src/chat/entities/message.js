class Message {
  constructor(data) {
    this.username = data.username;
    this.message = data.message;
    this.timesAsshole = data.timesAsshole;
    this.accountId = data.accountId;
    this.timeCreated = data.timeCreated;
    // TODO: Json parse should feed a constructor of type Metadata
    this.metadata = JSON.parse(data.metadata);
    this.flags = [];
  }
  setFlag(flag) {
    if (!this.flags.includes(flag)) {
      this.flags.push(flag);
    }
  }
  unsetFlag(flag) {
    if (this.flags.includes(flag)) {
      this.flags.splice(this.flags.indexOf(flag), 1);
    }
  }
  hasFlag(flag) {
    return this.flags.includes(flag);
  }
}

export default Message;
