let modTool;

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
        this.send("/app/mod/chat");
    }

    #onChatReceived(message) {
        if (message.content) {
            this.#chatUpdates = new ModChat(message.content, this);
            this.subscribe('/topic/mod/chat', (message) => this.#onChatUpdatesReceived(JSON.parse(message.body)));
        }
    }

    #onChatUpdatesReceived(message) {
        if (message) {
            this.#chatUpdates.update(message);
        }
    }

    getGameUpdates() {
        this.subscribe('/topic/mod/game', (message) => this.#onGameUpdatesReceived(JSON.parse(message.body)));
        this.subscribe('/topic/mod/global', (message) => this.#onGlobalUpdatesReceived(JSON.parse(message.body)));
    }

    #onGameUpdatesReceived(message) {
        if (message) {
            this.#gameUpdates = new ModGameEvents(message.content, this);
        }
    }

    #onGameUpdatesReceived(message) {
        if (!this.#gameUpdates) this.#gameUpdates = new ModGameEvents(message.content, this);

    }

    ban(event) {
        let dataSet = event.target.dataset;
        if (confirm(`Are you sure you want to ban ${dataSet.username}`)) {
            console.log(`Banning Account ${dataSet.accountId}`);
        }
    }

    mute(event) {
        let dataSet = event.target.dataset;
        if (confirm(`Are you sure you want to mute ${dataSet.username}`)) {
            console.log(`Muting Account ${dataSet.accountId}`);
        }
    }

    rename(event) {
        let dataSet = event.target.dataset;
        let newName = prompt(`What would you like to name ${dataSet.username}`);
        if (newName) {
            console.log(`Renaming ${dataSet.username} to ${newName}`);
        }
    }

    free(event) {
        let dataSet = event.target.dataset;
        if (confirm(`Are you sure you want to free ${dataSet.username}}`)) {
            console.log(`Freeing Account ${dataSet.accountId}`);
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
    #modTool
    #rowTemplate;
    #data;

    constructor(data, tool) {
        this.#modTool = tool;
        this.#rowTemplate = Handlebars.compile($('#messageRow-template').html());
        this.#data = data;
        this.#draw();
    }

    update(data) {
        this.#data.messages.unshift(data);
        this.#draw();
    }

    #draw() {
        this.#data.tags = tags;
        let html = this.#rowTemplate(this.#data);
        let messageBody = $('#messageBody')
        messageBody.html(html);
        $("*[id='banSymbol']").on('click', this.#modTool.ban.bind(this));
        $("*[id='muteSymbol']").on('click', this.#modTool.mute);
        $("*[id='nameSymbol']").on('click', this.#modTool.rename.bind(this));
        $("*[id='freeSymbol']").on('click', this.#modTool.free);
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