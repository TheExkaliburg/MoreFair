import { Client } from "@stomp/stompjs";
import { MentionMeta, MessageData } from "~/store/entities/message";

export enum LadderEventType {
  JOIN,
}

export type OnTickBody = {
  delta: number;
};
export type OnChatEventBody = MessageData;
export type OnLadderEventBody = {
  eventType: LadderEventType;
  accountId: number;
  data: any;
};
export type OnRoundEventBody = {};
export type OnAccountEventBody = {};

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

function addCallback(
  event: StompCallback<any>[],
  identifier: string,
  callback: (body: any) => void
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
  client.subscribe("/user/queue/round/event", (message) => {
    const body: OnRoundEventBody = JSON.parse(message.body);
    callbacks.onRoundEvent.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/ladder/event/1", (message) => {
    const body: OnLadderEventBody = JSON.parse(message.body);
    callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
  });
  client.subscribe("/user/queue/ladder/event", (message) => {
    const body: OnLadderEventBody = JSON.parse(message.body);
    callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/chat/event/1", (message) => {
    const body: OnChatEventBody = JSON.parse(message.body);
    callbacks.onChatEvent.forEach(({ callback }) => callback(body));
  });
  client.subscribe("/user/queue/chat/event", (message) => {
    const body: OnChatEventBody = JSON.parse(message.body);
    callbacks.onChatEvent.forEach(({ callback }) => callback(body));
  });
};

client.onStompError = (frame) => {
  console.log("Broker reported error: " + frame.headers.message);
  console.log("Additional details: " + frame.body);
};

const wsApi = (client: Client) => {
  return {
    account: {
      rename: () => {
        client.publish({
          destination: "/app/account/rename",
          body: JSON.stringify({ name: "test" }),
        });
      },
    },
    chat: {
      changeChat: (
        currentNumber: number,
        newNumber: number,
        unsubscribe?: boolean
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
        unsubscribe?: boolean
      ) => {
        if (unsubscribe) {
          client.unsubscribe(`/topic/ladder/event/${currentNumber}`);
        }
        client.subscribe(`/topic/ladder/event/${newNumber}`, (message) => {
          const body: OnLadderEventBody = JSON.parse(message.body);
          callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
        });
      },
      buyBias: () => {
        client.publish({
          destination: "/app/ladder/bias",
        });
      },
      buyMulti: () => {
        client.publish({
          destination: "/app/ladder/multi",
        });
      },
      throwVinegar: () => {
        client.publish({
          destination: "/app/ladder/vinegar",
        });
      },
      buyAutoPromote: () => {
        client.publish({
          destination: "/app/ladder/autopromote",
        });
      },
      promote: () => {
        client.publish({
          destination: "/app/ladder/promote",
        });
      },
    },
  };
};

export const useStomp = () => {
  // only activate the client if it is not already active
  while (!client.active) {
    client.activate();
  }

  return {
    wsApi: wsApi(client),
    callbacks,
    addCallback,
  };
};
