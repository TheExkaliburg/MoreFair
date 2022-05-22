import SockJs from "sockjs-client";
import Stomp from "stomp-websocket";
import Cookies from "js-cookie";

export class StompClient {
  constructor() {
    let isInDevelop = process.env.NODE_ENV === "development";
    let connection = isInDevelop
      ? "https://fair.kaliburg.de/fairsocket"
      : "/fairsocket";
    let socket = SockJs(connection);
    this.subscribeMap = new Map();
    this.stompClient = Stomp.over(socket);
    if (!isInDevelop) this.stompClient.debug = null;
  }

  connect(func) {
    this.stompClient.connect(
      {},
      () => {
        this.isFinished = new Promise((resolve) => {
          func(resolve);
        });
      },
      async () => {
        await this.disconnect();
      }
    );
  }

  async disconnect() {
    if (this.stompClient !== null) this.stompClient.disconnect();
    console.log(
      "Currently disconnected, waiting 120sec before trying to reconnect..."
    );
    await new Promise((r) => setTimeout(r, 120000));
    location.reload();
  }

  subscribe(destination, func) {
    let subscription = this.subscribeMap.get(destination);
    if (subscription) subscription.unsubscribe();
    subscription = this.stompClient.subscribe(
      destination,
      (message) => func(JSON.parse(message.body)),
      { uuid: Cookies.get("_uuid") }
    );
    this.subscribeMap.set(destination, subscription);
  }

  unsubscribe(destination) {
    let subscription = this.subscribeMap.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscribeMap.delete(destination);
    }
  }

  send(destination, payload) {
    let data = { uuid: Cookies.get("_uuid") };
    if (!data.uuid) data.uuid = "";
    if (payload) {
      if (payload.content) data.content = payload.content;
      if (payload.event) data.event = JSON.stringify(payload.event);
      if (payload.metadata) data.metadata = JSON.stringify(payload.metadata);
    }
    this.stompClient.send(destination, {}, JSON.stringify(data));
  }
}
