import { Client } from "@stomp/stompjs";

const isDevMode = process.env.NODE_ENV !== "production";
const connection = isDevMode
  ? "ws://localhost:8080/api/fairsocket"
  : `ws://${window.location.host}/api/fairsocket`;

const client = new Client({
  brokerURL: connection,
  debug: (str) => {
    if (isDevMode) console.debug(str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
});

client.onConnect = (frame) => {
  console.log("Connected: " + frame);
};

client.onStompError = (frame) => {
  console.log("Broker reported error: " + frame.headers.message);
  console.log("Additional details: " + frame.body);
};

const api = (client: Client) => {
  return {
    info: () => {
      client.publish({
        destination: "/app/info",
      });
    },
    account: {
      login: () => {
        console.log("login");
      },
    },
  };
};

export const useStomp = () => {
  // only activate the client if it is not already active
  if (!client.active) client.activate();

  return {
    api: api(client),
  };
};
