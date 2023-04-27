import { Client } from "@stomp/stompjs";
import { MentionMeta, MessageData } from "~/store/entities/message";
import { useAccountStore } from "~/store/account";
import { useChatStore } from "~/store/chat";

export enum LadderEventType {
  BUY_BIAS = "BUY_BIAS",
  BUY_MULTI = "BUY_MULTI",
  BUY_AUTO_PROMOTE = "BUY_AUTO_PROMOTE",
  THROW_VINEGAR = "THROW_VINEGAR",
  SOFT_RESET_POINTS = "SOFT_RESET_POINTS",
  PROMOTE = "PROMOTE",
  JOIN = "JOIN",
  ADD_FREE_AUTO = "ADD_FREE_AUTO",
}

export enum AccountEventType {
  BAN = "BAN",
  FREE = "FREE",
  MUTE = "MUTE",
  MOD = "MOD",
  NAME_CHANGE = "NAME_CHANGE",
}

export enum RoundEventType {
  RESET = "RESET",
  INCREASE_ASSHOLE_LADDER = "INCREASE_ASSHOLE_LADDER",
}

export type OnTickBody = {
  delta: number;
};
export type OnChatEventBody = MessageData;
export type OnLadderEventBody = {
  eventType: LadderEventType;
  accountId: number;
  data?: any;
};
export type OnRoundEventBody = {
  eventType: RoundEventType;
  data?: any;
};
export type OnAccountEventBody = {
  eventType: AccountEventType;
  accountId: number;
  data?: any;
};

export type StompCallback<T> = {
  callback: (body: T) => void;
  identifier: string;
};

export type StompCallbacks = {
  onTick: StompCallback<OnTickBody>[];
  onChatEvent: StompCallback<OnChatEventBody>[];
  onLadderEvent: StompCallback<OnLadderEventBody>[];
  onRoundEvent: StompCallback<OnRoundEventBody>[];
  onAccountEvent: StompCallback<OnAccountEventBody>[];
};

const callbacks: StompCallbacks = {
  onTick: [],
  onChatEvent: [],
  onLadderEvent: [],
  onRoundEvent: [],
  onAccountEvent: [],
};

function addCallback<T>(
  event: StompCallback<T>[],
  identifier: string,
  callback: (body: T) => void
) {
  const eventCallback = event.find((e) => e.identifier === identifier);
  if (eventCallback === undefined) {
    event.push({
      identifier,
      callback,
    });
  } else {
    eventCallback.callback = callback;
  }
}

let isPrivateConnectionEstablished = false;
const isDevMode = process.env.NODE_ENV !== "production";
const connection = isDevMode
  ? "ws://localhost:8080/socket/fair"
  : `ws://${window.location.host}/socket/fair`;

const client = new Client({
  brokerURL: connection,
  debug: (str) => {
    if (isDevMode) console.debug(str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
});

client.onConnect = (_) => {
  const accountStore = useAccountStore();
  client.subscribe("/topic/game/tick", (message) => {
    const body: OnTickBody = JSON.parse(message.body);
    callbacks.onTick.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/account/event", (message) => {
    const body: OnAccountEventBody = JSON.parse(message.body);
    callbacks.onAccountEvent.forEach(({ callback }) => callback(body));
  });
  client.subscribe("/user/queue/account/event", (message) => {
    const body: OnAccountEventBody = JSON.parse(message.body);
    callbacks.onAccountEvent.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/round/event", (message) => {
    const body: OnRoundEventBody = JSON.parse(message.body);
    callbacks.onRoundEvent.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/ladder/event/1", (message) => {
    const body: OnLadderEventBody = JSON.parse(message.body);
    callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/chat/event/1", (message) => {
    const body: OnChatEventBody = JSON.parse(message.body);
    callbacks.onChatEvent.forEach(({ callback }) => callback(body));
  });

  const privateUuid = accountStore.state.uuid;
  if (privateUuid !== "") {
    connectPrivateChannel(privateUuid);
  }
};

function connectPrivateChannel(uuid: string) {
  if (uuid === undefined || isPrivateConnectionEstablished) return;
  if (!client.connected) return;
  console.log("Reconnect private channel");
  isPrivateConnectionEstablished = true;

  client.subscribe(`/private/${uuid}/ladder/event`, (message) => {
    const body: OnLadderEventBody = JSON.parse(message.body);
    callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
  });
  client.subscribe(`/private/${uuid}/chat/event`, (message) => {
    const body: OnChatEventBody = JSON.parse(message.body);
    callbacks.onChatEvent.forEach(({ callback }) => callback(body));
  });
}

client.onStompError = (frame) => {
  console.log("Broker reported error: " + frame.headers.message);
  console.log("Additional details: " + frame.body);
};

client.onDisconnect = (_) => {
  isPrivateConnectionEstablished = false;
  useChatStore().actions.addSystemMessage(
    "You have been disconnected from the server, this could be because of a restart or an update. Please try reconnecting in a few minutes or try Discord if you cannot connect at all anymore."
  );
};

function reset() {
  client.deactivate().then();
}

function parseEvent(e: Event): string {
  return JSON.stringify(e);
}

const wsApi = (client: Client) => {
  return {
    account: {
      rename: (name: string) => {
        client.publish({
          destination: "/app/account/rename",
          body: JSON.stringify({ content: name }),
        });
      },
    },
    chat: {
      changeChat: (
        currentNumber: number,
        newNumber: number,
        unsubscribe: boolean = true
      ) => {
        if (unsubscribe) {
          client.unsubscribe(`/topic/chat/event/${currentNumber}`);
        }
        client.subscribe(`/topic/chat/event/${newNumber}`, (message) => {
          const body: OnChatEventBody = JSON.parse(message.body);
          callbacks.onChatEvent.forEach(({ callback }) => callback(body));
        });
      },
      sendMessage: (
        message: string,
        metadata: MentionMeta[],
        chatNumber: number
      ) => {
        client.publish({
          destination: `/app/chat/${chatNumber}`,
          body: JSON.stringify({
            content: message,
            metadata: JSON.stringify(metadata),
          }),
        });
      },
    },
    ladder: {
      changeLadder: (
        currentNumber: number,
        newNumber: number,
        unsubscribe: boolean = true
      ) => {
        if (unsubscribe) {
          client.unsubscribe(`/topic/ladder/event/${currentNumber}`);
        }
        client.subscribe(`/topic/ladder/event/${newNumber}`, (message) => {
          const body: OnLadderEventBody = JSON.parse(message.body);
          callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
        });
      },
      buyBias: (e: Event) => {
        client.publish({
          destination: "/app/ladder/bias",
          body: JSON.stringify({
            event: parseEvent(e),
          }),
        });
      },
      buyMulti: (e: Event) => {
        client.publish({
          destination: "/app/ladder/multi",
          body: JSON.stringify({
            event: parseEvent(e),
          }),
        });
      },
      throwVinegar: (e: Event) => {
        client.publish({
          destination: "/app/ladder/vinegar",
          body: JSON.stringify({
            event: parseEvent(e),
          }),
        });
      },
      buyAutoPromote: (e: Event) => {
        client.publish({
          destination: "/app/ladder/autopromote",
          body: JSON.stringify({
            event: parseEvent(e),
          }),
        });
      },
      promote: (e: Event) => {
        client.publish({
          destination: "/app/ladder/promote",
          body: JSON.stringify({
            event: parseEvent(e),
          }),
        });
      },
    },
  };
};

export const useStomp = () => {
  // only activate the client if it is not already active
  if (!client.active) {
    client.activate();
  }

  return {
    wsApi: wsApi(client),
    callbacks,
    addCallback,
    connectPrivateChannel,
    reset,
  };
};
