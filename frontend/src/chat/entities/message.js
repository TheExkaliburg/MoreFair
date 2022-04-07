class Message {
  constructor(data) {
    this.username = data.username;
    this.message = data.message;
    this.timesAsshole = data.timesAsshole;
    this.accountId = data.accountId;
    this.timeCreated = data.timeCreated;
    // TODO: Json parse should feed a constructor of type Metadata
    this.metadata = JSON.parse(data.metadata);
  }
}

export default Message;
