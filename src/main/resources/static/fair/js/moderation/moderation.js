let modInfo = {
    highestLadder: 1
}

let modTool;

function setup() {
    modTool = new ModerationTool(true);
}

class ModerationTool {
    #stompClient;
    #subscribeMap;
    #modInfo;
    #chatUpdates;
    #gameUpdates;

    constructor(enableDebug) {
        let socket = new SockJS('/fairsocket');
        this.#subscribeMap = new Map();
        this.#stompClient = Stomp.over(socket);
        if (!enableDebug) this.#stompClient.debug = null;
        this.#stompClient.connect({}, (frame) => {
            // INFO
            this.getInfo();
        }, async function (frame) {
            //await handleReset();
        });
    }

    subscribe(destination, func) {
        let subscription = this.#subscribeMap.get(destination);
        if (subscription) {
            subscription.unsubscribe();
        }
        subscription = this.#stompClient.subscribe(destination, (message) => func(message), {uuid: getCookie("_uuid")});
        this.#subscribeMap.set(destination, subscription);
    }

    send(destination) {
        this.send(destination, null);
    }

    send(destination, content) {
        let data = {uuid: getCookie("_uuid")}
        if (content) data.content = content;
        this.#stompClient.send(destination, {}, JSON.stringify(data))
    }

    disconnect() {
        if (this.#stompClient !== null) this.#stompClient.disconnect();
        console.log("Currently disconnected...");
    }

    getInfo() {
        this.subscribe('/user/queue/mod/info', (message) => this.#onInfoReceived(JSON.parse(message.body)));
        this.send("/app/mod/info");
    }

    #onInfoReceived(message) {
        if (message.content) {
            this.#modInfo = new ModInfo(message.content);
            this.getChatUpdates();
            this.getGameUpdates();
        }
    }

    getChatUpdates() {
        this.subscribe('/user/queue/mod/chat', (message) => this.#onChatReceived(JSON.parse(message.body)));
        this.subscribe('/topic/mod/chat', (message) => this.#onChatUpdatesReceived(JSON.parse(message.body)));
        this.send("/app/mod/chat");
    }

    #onChatReceived(message) {
        if (message.content) {
            this.#chatUpdates = new ModGameEvents(message.content);
        }
    }

    #onChatUpdatesReceived(message) {
        if (message.content) {
            this.#chatUpdates.update(message.content);
        }
    }

    getGameUpdates() {
        this.subscribe('/topic/mod/game', (message) => this.#onGameUpdatesReceived(JSON.parse(message.body)));
    }

    #onGameUpdatesReceived(message) {
        if (message.content) {
            this.#gameUpdates = new ModGameEvents(message.content);
        }
    }
}

class ModInfo {
    highestLadder;
    yourAccessRole;

    constructor(data) {
        this.highestLadder = data.highestLadder;
        this.yourAccessRole = data.yourAccessRole;
    }
}

class ModChat {
    #dom;

    constructor(data) {
        console.log(data);
    }

    update(data) {

    }
}

class ModGameEvents {
    #dom;

    constructor(data) {
        console.log(data);
    }

    update(data) {

    }
}