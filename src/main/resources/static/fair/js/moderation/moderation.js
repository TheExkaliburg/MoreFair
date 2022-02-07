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
            for (let i = 1; i <= this.#modInfo.highestLadder; i++) {
                this.subscribe('/topic/chat/' + i, (message) => this.#onChatUpdatesReceived(JSON.parse(message.body)));
            }
        }
    }

    #onChatUpdatesReceived(message) {
        if (message) {
            this.#chatUpdates.update(message);
        }
    }

    getGameUpdates() {
        this.subscribe('/user/queue/ladder/', (message) => this.#onGameReceived(JSON.parse(message.body)));
        this.send("/app/ladder/init/" + 1);
    }

    #onGameReceived(message) {
        if (message.content) {
            this.#gameUpdates = new ModGameEvents(message.content, this);
            for (let i = 1; i <= this.#modInfo.highestLadder; i++) {
                this.subscribe('/topic/ladder/' + i, (message) => this.#onGameUpdatesReceived(JSON.parse(message.body), i));
            }

            this.subscribe('/topic/global/', (message) => this.#onGameUpdatesReceived({events: JSON.parse(message.body)}, 0));
            this.subscribe('/topic/mod/', (message) => this.#onGameUpdatesReceived({events: JSON.parse(message.body)}, 0));
        }
    }

    #onGameUpdatesReceived(message, ladder) {
        if (message) {
            this.#gameUpdates.update(message, ladder);
        }
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
        let html = this.#rowTemplate(this.#data);
        let messageBody = $('#messageBody');
        messageBody.html(html);
        console.log($("#messageBody .banSymbol"));
        $("#messageBody .banSymbol").on('click', this.#modTool.ban);
        $("#messageBody .muteSymbol").on('click', this.#modTool.mute);
        $("#messageBody .nameSymbol").on('click', this.#modTool.rename);
        $("#messageBody .freeSymbol").on('click', this.#modTool.free);
    }


}

class ModGameEvents {
    #rowTemplate;
    #modTool
    #data
    #rankers

    constructor(data, tool) {
        this.#modTool = tool;
        this.#rankers = data.rankers;
        this.#rowTemplate = Handlebars.compile($('#updateRow-template').html())
        this.#data = {events: []};
        this.#draw();
    }

    update(data, ladder) {
        data.events.forEach(event => {
            if (event.eventType === "JOIN" && ladder === 1) {
                let newRanker = {
                    accountId: event.accountId,
                    username: event.data.username,
                    timesAsshole: event.data.timesAsshole,
                }
                this.#rankers.unshift(newRanker);
            }
            let ranker;
            this.#rankers.forEach(r => {
                if (r.accountId === event.accountId) {
                    event.username = r.username;
                    ranker = r;
                }
            });

            event.ladder = ladder;

            switch (event.eventType) {
                case 'PROMOTE':
                case 'VINEGAR':
                case 'AUTO_PROMOTE':
                case 'NAME_CHANGE':
                    console.log(event);
                    this.#data.events.unshift(event);
                    break;
            }

            if (event.eventType === "NAME_CHANGE") {
                ranker.username = event.data;
            }
        });
        this.#draw();
    }

    #draw() {
        let html = this.#rowTemplate(this.#data);
        let updateBody = $('#updateBody');
        updateBody.html(html);
    }

}