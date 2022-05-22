let modTool;

let loggedEvents = ['NAME_CHANGE', 'BAN', 'MUTE', 'FREE', 'MOD', 'CONFIRM'];
let filterLocations = [];
let filterLadders = [];
let filterAccounts = [];

class ModerationTool {
  #stompClient;
  #subscribeMap;
  #chatUpdates;
  #gameUpdates;
  #modInfo;

  constructor(enableDebug) {
    let socket = new SockJS('/fairsocket');
    this.#subscribeMap = new Map();
    this.#stompClient = Stomp.over(socket);
    if (!enableDebug) {
      this.#stompClient.debug = null;
    }
    this.#stompClient.connect({}, (frame) => {
      // INFO
      this.getInfo();
    }, async function (frame) {
      await this.disconnect();
    });
  }

  subscribe(destination, func) {
    let subscription = this.#subscribeMap.get(destination);
    if (subscription) {
      subscription.unsubscribe();
    }
    subscription = this.#stompClient.subscribe(destination,
        (message) => func(message), {uuid: getCookie("_uuid")});
    this.#subscribeMap.set(destination, subscription);
  }

  send(destination) {
    this.send(destination, null);
  }

  send(destination, content) {
    let data = {uuid: getCookie("_uuid")}
    if (content) {
      data.content = content;
    }
    this.#stompClient.send(destination, {}, JSON.stringify(data))
  }

  async disconnect() {
    if (this.#stompClient !== null) {
      this.#stompClient.disconnect();
    }
    console.log("Currently disconnected...");
    await new Promise((r) => setTimeout(r, 65000));
    location.reload();
  }

  getInfo() {
    this.subscribe('/user/queue/mod/info/',
        (message) => this.#onInfoReceived(JSON.parse(message.body)));
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
    this.subscribe('/user/queue/mod/chat/',
        (message) => this.#onChatReceived(JSON.parse(message.body)));
    this.send("/app/mod/chat");
  }

  #onChatReceived(message) {
    if (message.content) {
      this.#chatUpdates = new ModChat(message.content, this);
      for (let i = 1; i <= this.#modInfo.highestLadder; i++) {
        this.subscribe('/topic/chat/' + i,
            (message) => this.#onChatUpdatesReceived(JSON.parse(message.body),
                i));
      }
    }
  }

  #onChatUpdatesReceived(message, ladder) {
    if (message) {
      this.#chatUpdates.update(message, ladder);
    }
  }

  getGameUpdates() {
    this.subscribe('/user/queue/ladder/',
        (message) => this.#onGameReceived(JSON.parse(message.body)));
    this.send("/app/ladder/init/" + 1);
  }

  #onGameReceived(message) {
    if (message.content) {
      this.#gameUpdates = new ModGameEvents(message.content, this);
      for (let i = 1; i <= this.#modInfo.highestLadder; i++) {
        this.subscribe('/topic/ladder/' + i,
            (message) => this.#onGameUpdatesReceived(JSON.parse(message.body),
                i));
        this.subscribe('/topic/mod/ladder/' + i,
            (message) => this.#onDestinationUpdatesReceived(
                JSON.parse(message.body), i));
      }
      this.subscribe('/topic/global/', (message) => this.#onGameUpdatesReceived(
          {events: JSON.parse(message.body)}, 0));
    }
  }

  #onGameUpdatesReceived(message, ladder) {
    if (message) {
      this.#gameUpdates.update(message, ladder);
    }
  }

  #onDestinationUpdatesReceived(message, ladder) {
    if (message) {
      this.#gameUpdates.destinationUpdate(message, ladder);
    }
  }

  ban(event) {
    let dataSet = event.target.dataset;
    if (confirm(
        `Are you sure you want to ban "${dataSet.username}" (${dataSet.accountId})`)) {
      this.send("/app/mod/ban/" + dataSet.accountId);
    }
  }

  mute(event) {
    let dataSet = event.target.dataset;
    if (confirm(
        `Are you sure you want to mute "${dataSet.username}" (${dataSet.accountId})`)) {
      this.send("/app/mod/mute/" + dataSet.accountId);
    }
  }

  rename(event) {
    let dataSet = event.target.dataset;
    let newName = prompt(
        `What would you like to name "${dataSet.username}" (${dataSet.accountId})`);
    if (newName) {
      this.send("/app/mod/name/" + dataSet.accountId, newName);
    }
  }

  promptConfirm(event) {
    let dataSet = event.target.dataset;
    let text = prompt(
        `What confirm-prompt would you like to send to "${dataSet.username}" (${dataSet.accountId})`);
    if (text) {
      this.send("/app/mod/confirm/" + dataSet.accountId, text);
    }
  }

  free(event) {
    let dataSet = event.target.dataset;
    if (confirm(
        `Are you sure you want to free "${dataSet.username}" (${dataSet.accountId})`)) {
      this.send("/app/mod/free/" + dataSet.accountId);
    }
  }

  mod(event) {
    let dataSet = event.target.dataset;
    if (confirm(
        `Are you sure you want to mod "${dataSet.username}" (${dataSet.accountId})`)) {
      this.send("/app/mod/mod/" + dataSet.accountId);
    }
  }

  getModInfo() {
    return this.#modInfo;
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

  update(data, ladder) {
    data.ladderNumber = ladder;
    this.#data.messages.unshift(data);
    if (this.#data.messages.length > 300) {
      this.#data.messages.pop();
    }
    this.#draw();
  }

  #draw() {
    let html = this.#rowTemplate(this.#data);
    let messageBody = $('#messageBody');
    messageBody.html(html);
    $("#messageBody .banSymbol").on('click',
        this.#modTool.ban.bind(this.#modTool));
    $("#messageBody .muteSymbol").on('click',
        this.#modTool.mute.bind(this.#modTool));
    $("#messageBody .nameSymbol").on('click',
        this.#modTool.rename.bind(this.#modTool));
    $("#messageBody .freeSymbol").on('click',
        this.#modTool.free.bind(this.#modTool));
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

    $("#lookupButton").on('click', this.find.bind(this));
    $("#idButton").on('click', this.forceEvent.bind(this));
  }

  update(data, ladder) {
    let isUpdated = false;
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
          if (event.eventType === 'NAME_CHANGE') {
            r.username = event.data;
          }
        }
      });

      event.ladder = ladder;
      let time = new Date(Date.now());
      event.timeCreated = days[time.getDay()] + " " +
          [time.getHours().toString().padStart(2, '0'),
            time.getMinutes().toString().padStart(2, '0'),].join(':');

      if (loggedEvents.includes(event.eventType)) {
        this.#data.events.unshift(event);
        if (this.#data.events.length > 50) {
          this.#data.events.pop();
        }
        isUpdated = true;
      }

      if (event.eventType === "NAME_CHANGE") {
        ranker.username = event.data;
      }
    });
    if (isUpdated) {
      this.#draw();
    }
  }

  destinationUpdate(data, ladder) {
    let event = {}
    event.accountId = data.id;
    this.#rankers.forEach(r => {
      if (r.accountId === event.accountId) {
        event.username = r.username;
      }
    });
    event.ladder = ladder;
    let time = new Date(Date.now());
    event.timeCreated = days[time.getDay()] + " " +
        [time.getHours().toString().padStart(2, '0'),
          time.getMinutes().toString().padStart(2, '0'),].join(':');

    event.data = {};
    if (data.content) {
      event.data.content = JSON.parse(data.content);
    }
    event.data.event = JSON.parse(data.event);

    if (data.location.includes('app/ladder/')) {
      event.eventType = data.location.slice(('app/ladder/').length);
    } else {
      event.eventType = data.location;
    }

    let isInFilter = true;
    if (filterAccounts.length > 0 && !filterAccounts.includes(
        data.id)) {
      isInFilter = false;
    }
    if (filterLadders.length > 0 && !filterLadders.includes(
        ladder)) {
      isInFilter = false;
    }

    if (filterLocations.includes(data.location) && isInFilter) {
      this.#data.events.unshift(event);
      if (this.#data.events.length > 50) {
        this.#data.events.pop();
      }
      this.#draw();
    }
  }

  #draw() {
    let html = this.#rowTemplate(this.#data);
    let updateBody = $('#updateBody');
    updateBody.html(html);
    $("#updateBody .banSymbol").on('click',
        this.#modTool.ban.bind(this.#modTool));
    $("#updateBody .muteSymbol").on('click',
        this.#modTool.mute.bind(this.#modTool));
    $("#updateBody .nameSymbol").on('click',
        this.#modTool.rename.bind(this.#modTool));
    $("#updateBody .confirmSymbol").on('click',
        this.#modTool.promptConfirm.bind(this.#modTool));
    $("#updateBody .freeSymbol").on('click',
        this.#modTool.free.bind(this.#modTool));
    if (this.#modTool.getModInfo().yourAccessRole === "OWNER") {
      $("#updateBody .modSymbol").on('click',
          this.#modTool.mod.bind(this.#modTool));
    } else {
      $("#updateBody .modSymbol").remove();
    }
  }

  find() {
    let input = $('#usernameLookup');
    let result = '['

    this.#rankers.forEach(r => {
      if (r.username.toLowerCase().includes(input.val().toLowerCase())) {
        result += '{' + r.accountId + ':' + r.username + '} ';
      }
    });

    result = result.trim() + ']';
    $('#lookupResult').html(result);
  }

  forceEvent() {
    let input = $('#idInput');

    let forcedEvent = {
      ladder: 0,
      accountId: parseInt(input.val().trim()),
      eventType: "FORCED"
    }
    this.#rankers.forEach(r => {
      if (r.accountId === forcedEvent.accountId) {
        forcedEvent.username = r.username;
      }
    });

    let time = new Date(Date.now());
    forcedEvent.timeCreated = days[time.getDay()] + " " +
        [time.getHours().toString().padStart(2, '0'),
          time.getMinutes().toString().padStart(2, '0'),].join(':');

    this.#data.events.unshift(forcedEvent);
    this.#draw();
  }
}

let days = ['Su.', 'Mo.', 'Tu.', 'We.', 'Th.', 'Fr.', 'Sa.'];