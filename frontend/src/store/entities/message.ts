export enum MessagePartType {
  plain,
  mentionUser,
  mentionUserId,
  mentionGroup,
}

export type UserMentionMeta = {
  u: string;
  i: number;
  id: number;
};

export type GroupMentionMeta = {
  g: string;
  i: number;
};

export type MentionMeta = GroupMentionMeta | UserMentionMeta;

function isGroupMentionMeta(meta: MentionMeta): meta is GroupMentionMeta {
  return "g" in meta && "i" in meta;
}

export class MessagePart {
  public readonly type: MessagePartType;
  public readonly text: string;

  constructor(type: MessagePartType, text: string) {
    this.type = type;
    this.text = text;
  }

  is(type) {
    if (this.type === type) return true;
  }
}

export type MessageData = {
  id: number;
  username: string;
  message: string;
  metadata: string;
  timestamp: number;
  tag: string;
  assholePoints: number;
};

export class Message implements MessageData {
  id: number = 0;
  username: string = "";
  message: string = "";
  metadata: string = "[]";
  timestamp: number = 0;
  tag: string = "";
  assholePoints: number = 0;
  private flags: string[] = [];

  constructor(data: any) {
    Object.assign(this, data);
  }

  setFlag(flag: string) {
    if (!this.hasFlag(flag)) {
      this.flags.push(flag);
    }
  }

  removeFlag(flag: string) {
    if (this.hasFlag(flag)) {
      this.flags.splice(this.flags.indexOf(flag), 1);
    }
  }

  hasFlag(flag: string) {
    return this.flags.includes(flag);
  }

  getMetadata(): MentionMeta[] {
    return JSON.parse(this.metadata);
  }

  getTimestampString() {
    const date = new Date(0);
    date.setUTCSeconds(this.timestamp);
    // format to weekday. hours:minutes
    return date.toLocaleString("en-UK", {
      weekday: "short",
      hour: "numeric",
      minute: "numeric",
    });
  }

  getMessageParts(): MessagePart[] {
    const message = this.message;
    const metadata = this.getMetadata();
    const result: MessagePart[] = [];

    if (metadata.length === 0) {
      return [new MessagePart(MessagePartType.plain, message)];
    }

    const combinedMentions = metadata.sort((a, b) => a.i - b.i);

    let lastIndex = 0;
    combinedMentions.forEach((m) => {
      const index = m.i;
      if (isGroupMentionMeta(m)) {
        let name = m.g;
        name = name.trim();
        if (message.slice(index, index + 3) !== "{$}") return;

        result.push(
          new MessagePart(
            MessagePartType.plain,
            message.slice(lastIndex, index)
          )
        );
        result.push(new MessagePart(MessagePartType.mentionGroup, name));
        lastIndex = index + 3;
      } else {
        const name = m.u.trim();
        const id = m.id;
        if (message.slice(index, index + 3) !== "{@}") return;

        result.push(
          new MessagePart(
            MessagePartType.plain,
            message.slice(lastIndex, index)
          )
        );
        result.push(new MessagePart(MessagePartType.mentionUser, name));
        result.push(new MessagePart(MessagePartType.mentionUserId, String(id)));
        lastIndex = index + 3;
      }
    });

    // Take the last part and add it as Plain text
    result.push(
      new MessagePart(MessagePartType.plain, message.slice(lastIndex))
    );

    return result;
  }
}

/*
const m = new Message({
  id: 1,
  username: "test",
  message: "test {$} {@} test",
  metadata: '[{"g":"group","i":5},{"u":"user","i":9,"id":1}]',
  timestamp: 0,
  tag: "",
  assholePoints: 0,
});

console.log(m.getMessageParts());
*/
