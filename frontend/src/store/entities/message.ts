import { ChatType } from "~/store/chat";
import { useLang } from "~/composables/useLang";

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

export function isGroupMentionMeta(
  meta: MentionMeta,
): meta is GroupMentionMeta {
  return "g" in meta && "i" in meta;
}

export class MessagePart {
  public readonly type: MessagePartType;
  public readonly text: string;

  constructor(type: MessagePartType, text: string) {
    this.type = type;
    this.text = text;
  }

  is(type: MessagePartType) {
    if (this.type === type) return true;
  }
}

export type MessageData = {
  accountId: number;
  username: string;
  message: string;
  metadata: string;
  timestamp: number;
  tag: string;
  assholePoints: number;
  ladderNumber: number;
  isMod: boolean;
  chatType: ChatType;
};

// Set the options for the Intl.DateTimeFormat object
const options: Intl.DateTimeFormatOptions = {
  weekday: "short",
  hour: "numeric",
  minute: "numeric",
  hourCycle: "h23",
};

// Create the Intl.DateTimeFormat object with the client's default locale
const formatter = new Intl.DateTimeFormat(navigator.language, options);

export class Message implements MessageData {
  accountId = 0;
  username = "";
  message = "";
  ladderNumber = 0;
  metadata = "[]";
  timestamp = 0;
  tag = "";
  assholePoints = 0;
  isMod = false;
  chatType = ChatType.GLOBAL;
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
    // Create a date object with the timestamp
    const date = new Date(0);
    date.setUTCSeconds(this.timestamp);

    // Format the date using the formatter object
    return formatter.format(date);
  }

  getChatTypeIdentifier(): string {
    const lang = useLang("chat");
    let result = lang(this.chatType.toUpperCase() + ".identifier");
    if (this.chatType === ChatType.LADDER) {
      result += this.ladderNumber;
    }
    return result;
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
            message.slice(lastIndex, index),
          ),
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
            message.slice(lastIndex, index),
          ),
        );
        result.push(new MessagePart(MessagePartType.mentionUser, name));
        result.push(new MessagePart(MessagePartType.mentionUserId, String(id)));
        lastIndex = index + 3;
      }
    });

    // Take the last part and add it as Plain text
    result.push(
      new MessagePart(MessagePartType.plain, message.slice(lastIndex)),
    );

    return result;
  }
}
