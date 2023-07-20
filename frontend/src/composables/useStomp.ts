import { Client, StompSubscription } from "@stomp/stompjs";
import { createPinia, getActivePinia, setActivePinia } from "pinia";
import { MentionMeta, MessageData } from "~/store/entities/message";
import { AccountEventType, useAccountStore } from "~/store/account";
import { useChatStore } from "~/store/chat";
import { RoundEventType } from "~/store/round";
import { LadderEventType } from "~/store/ladder";
import { ChatLogMessageData } from "~/store/moderation";

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

export type OnModChatEventBody = OnChatEventBody & ChatLogMessageData;
export type OnModLogEventBody = {};

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
  onModChatEvent: StompCallback<OnModChatEventBody>[];
  onModLogEvent: StompCallback<OnModLogEventBody>[];
};

const callbacks: StompCallbacks = {
  onTick: [],
  onChatEvent: [],
  onLadderEvent: [],
  onRoundEvent: [],
  onAccountEvent: [],
  onModChatEvent: [],
  onModLogEvent: [],
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

if (global !== undefined && global.WebSocket === undefined) {
  Object.assign(global, { WebSocket: (await import("ws")).WebSocket });
}

let isPrivateConnectionEstablished = false;
const isDevMode = process.env.NODE_ENV !== "production";
const connectionType = window.location.protocol === "https:" ? "wss" : "ws";
const connection = isDevMode
  ? "ws://localhost:8080/socket/fair"
  : `${connectionType}://${window.location.host}/socket/fair`;

const disconnectMessage =
  "You have been disconnected from the server, this could be because of a restart or an update. Please try reconnecting in a few minutes or try Discord if you cannot connect at all anymore.";
const reconnectTimeout = 1 * 60 * 1000;

const subscribedChannel = {
  ladder: {} as StompSubscription,
  chat: {} as StompSubscription,
};

const client = new Client({
  brokerURL: connection,
  debug: (_) => {
    // if (isDevMode) console.debug(str);
  },
  reconnectDelay: 0,
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

  client.subscribe("/topic/round/event", (message) => {
    const body: OnRoundEventBody = JSON.parse(message.body);
    callbacks.onRoundEvent.forEach(({ callback }) => callback(body));
  });

  if (getActivePinia() === undefined) {
    setActivePinia(createPinia());
  }

  const accountStore = useAccountStore();
  const privateUuid = accountStore.state.uuid;
  if (privateUuid !== "") {
    connectPrivateChannel(privateUuid);
  }
};

function connectPrivateChannel(uuid: string) {
  if (uuid === undefined || isPrivateConnectionEstablished) return;
  if (!client.connected) return;
  isPrivateConnectionEstablished = true;

  const highestLadder = useAccountStore().state.highestCurrentLadder;

  subscribedChannel.ladder = client.subscribe(
    "/topic/ladder/event/" + highestLadder,
    (message) => {
      const body: OnLadderEventBody = JSON.parse(message.body);
      callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
    }
  );

  subscribedChannel.chat = client.subscribe(
    "/topic/chat/event/" + highestLadder,
    (message) => {
      const body: OnChatEventBody = JSON.parse(message.body);
      callbacks.onChatEvent.forEach(({ callback }) => callback(body));
    }
  );

  client.subscribe(`/private/${uuid}/ladder/event`, (message) => {
    const body: OnLadderEventBody = JSON.parse(message.body);
    callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
  });
  client.subscribe(`/private/${uuid}/chat/event`, (message) => {
    const body: OnChatEventBody = JSON.parse(message.body);
    callbacks.onChatEvent.forEach(({ callback }) => callback(body));
  });
  client.subscribe(`/private/${uuid}/account/event`, (message) => {
    const body: OnAccountEventBody = JSON.parse(message.body);
    callbacks.onAccountEvent.forEach(({ callback }) => callback(body));
  });

  connectModeratorChannel();
}

function connectModeratorChannel() {
  if (!client.connected) return;
  if (!useAccountStore().getters.isMod) return;

  client.subscribe("/topic/moderation/chat/event", (message) => {
    const body: OnModChatEventBody = JSON.parse(message.body);
    callbacks.onModChatEvent.forEach(({ callback }) => callback(body));
  });

  client.subscribe("/topic/moderation/log/event", (message) => {
    const body: OnModLogEventBody = JSON.parse(message.body);
    callbacks.onModLogEvent.forEach(({ callback }) => callback(body));
  });
}

client.onStompError = (_) => {
  // gets called when can't establish connection
  isPrivateConnectionEstablished = false;
  setTimeout(() => {
    reset();
  }, 5 * 1000);
};

client.onWebSocketClose = (_) => {
  // gets called when the connection is lost/server is restarted or when the client disconnects by itself
  isPrivateConnectionEstablished = false;
  useToasts(disconnectMessage, { type: "error" });
  useChatStore().actions.addSystemMessage(disconnectMessage);
  setTimeout(() => {
    window.location.reload();
  }, reconnectTimeout);
};

/* client.onDisconnect = (_) => {
  // gets called when the client disconnects by itself through script, but before the onWebSocketClose
  console.log("disconnected");
  isPrivateConnectionEstablished = false;
  useChatStore().actions.addSystemMessage(disconnectMessage);
  setTimeout(() => {
    window.location.reload();
  }, reconnectTimeout);
}; */

function reset() {
  client.deactivate().then();
}

function parseEvent(e: Event): string {
  const serializableEvent = {
    isTrusted: e.isTrusted,
    // @ts-ignore
    screenX: e.screenX ?? -1,
    // @ts-ignore
    screenY: e.screenY ?? -1,
  };

  if (!(e instanceof Event)) {
    serializableEvent.isTrusted = false;
  }

  return JSON.stringify(serializableEvent);
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
      changeChat: (newNumber: number) => {
        if (subscribedChannel?.chat) {
          subscribedChannel.chat.unsubscribe();
        }
        subscribedChannel.chat = client.subscribe(
          `/topic/chat/event/${newNumber}`,
          (message) => {
            const body: OnChatEventBody = JSON.parse(message.body);
            callbacks.onChatEvent.forEach(({ callback }) => callback(body));
          }
        );
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
      changeLadder: (newNumber: number) => {
        if (subscribedChannel?.ladder) {
          subscribedChannel.ladder.unsubscribe();
        }
        subscribedChannel.ladder = client.subscribe(
          `/topic/ladder/event/${newNumber}`,
          (message) => {
            const body: OnLadderEventBody = JSON.parse(message.body);
            callbacks.onLadderEvent.forEach(({ callback }) => callback(body));
          }
        );
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
          destination: "/app/ladder/autoPromote",
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
    moderation: {
      ban: (accountId: number) => {
        client.publish({
          destination: `/app/moderation/ban/${accountId}`,
        });
      },
      mute: (accountId: number) => {
        client.publish({
          destination: `/app/moderation/mute/${accountId}`,
        });
      },
      rename: (accountId: number, username: string) => {
        client.publish({
          destination: `/app/moderation/rename/${accountId}`,
          body: username,
        });
      },
      free: (accountId: number) => {
        client.publish({
          destination: `/app/moderation/free/${accountId}`,
        });
      },
      mod: (accountId: number) => {
        client.publish({
          destination: `/app/moderation/mod/${accountId}`,
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
