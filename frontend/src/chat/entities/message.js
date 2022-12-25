class Message {
  constructor(data) {
    this.username = data.username;
    this.message = data.message;
    this.tag = data.tag;
    this.ahPoints = data.ahPoints;
    this.accountId = data.accountId;
    this.timestamp = data.timestamp;
    // TODO: Json parse should feed a constructor of type Metadata
    try {
      this.metadata = JSON.parse(data.metadata);
    } catch (e) {
      this.metadata = [];
    }
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

  getTimestampString() {
    // Create a date object with the timestamp
    const date = new Date(0);
    date.setUTCSeconds(this.timestamp);

    // Get the timezone offset of the client's timezone in minutes
    const timezoneOffset = date.getTimezoneOffset();

    // Adjust the timestamp by the timezone offset
    date.setUTCMinutes(date.getUTCMinutes() + timezoneOffset);

    const options = {
      weekday: "short",
      hour: "numeric",
      minute: "numeric",
    };

    const formatter = new Intl.DateTimeFormat(undefined, options);
    return formatter.format(date);
  }
}

export default Message;
