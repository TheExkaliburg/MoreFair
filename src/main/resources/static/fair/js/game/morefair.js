let infoData = {
    pointsForPromote: new Decimal(250000000),
    minimumPeopleForPromote: 10,
    assholeLadder: 15,
    assholeTags: [''],
    baseVinegarNeededToThrow: new Decimal(1000000),
    baseGrapesNeededToAutoPromote: new Decimal(2000),
    manualPromoteWaitTime: 15,
    autoPromoteLadder: 2
}

let clientData = {
    ladderAreaSize: 1,
    ladderPadding: 7
}

let numberFormatter;

let stompClient = null;
let chatSubscription = null;
let ladderSubscription = null;

let isQolLoaded = false;

function connect() {
    let socket = new SockJS('/fairsocket');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function (frame) {
        // INFO
        stompClient.subscribe('/user/queue/info',
            (message) => onInfoReceived(JSON.parse(message.body)), {uuid: getCookie("_uuid")})
        getInfo()
    }, async function (frame) {
        await handleReset();
    })
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Currently disconnected...");
}


function loadAndRunScript(url) {
    var script = document.createElement('script');
    script.src = url;
    document.getElementsByTagName('head')[0].appendChild(script);
}

function loadQOLScripts(skipConfirm = false) {
    if (isQolLoaded || (!skipConfirm && !confirm("The QOL scripts are written and maintained by a 3rd-party, they are not guaranteed to work and may cause issues or break things. Do you want to continue loading these?"))) {
        return;
    }

    //Closing the navbar because it would cause issues.
    if(!skipConfirm)
    {
        document.getElementsByClassName("navbar-toggler")[0].click();
    }
    // Main QOL Script
    loadAndRunScript("https://raw.githack.com/LynnCinnamon/fair-game-qol/main/fairgame.js");
    // Lynn's Addon
    loadAndRunScript("https://raw.githack.com/LynnCinnamon/Fairgame-Lynns-QOL-Extensions/master/Lynns%20Extension.js");
    isQolLoaded = true;
}

async function setup() {
    numberFormatter = new numberformat.Formatter({
        format: 'hybrid',
        sigfigs: 6,
        flavor: 'short',
        minSuffix: 1e10,
        maxSmall: 0
    });
    connect();

    $('#messageInput')[0].addEventListener("keyup", event => {
        if (event.key === "Enter") {
            // Cancel the default action, if needed
            event.preventDefault();
            // Trigger the button element with a click
            sendMessage();
        }
    });

    // For a better script updating experience.
    // With this, the script may enable the user to reload the script when a new version is available.
    // (See https://github.com/LynnCinnamon/Fairgame-Lynns-QOL-Extensions/commit/f00d896a241d46b6153558256baa5139c9f350c5)
    if(localStorage.getItem("autoLoadQOL") === "true")
    {
        localStorage.removeItem("autoLoadQOL");
        loadQOLScripts(true);
    }
}

function onInfoReceived(message) {
    if (message.content) {
        infoData = message.content
        infoData.pointsForPromote = new Decimal(infoData.pointsForPromote);
        infoData.baseVinegarNeededToThrow = new Decimal(infoData.baseVinegarNeededToThrow);
        infoData.baseGrapesNeededToAutoPromote = new Decimal(infoData.baseGrapesNeededToAutoPromote);

        // Login
        stompClient.subscribe('/user/queue/account/login',
            (message) => onLoginReceived(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        login()
    }
}

function getInfo() {
    stompClient.send("/app/info", {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function login() {
    let uuid = getCookie("_uuid")
    if (uuid === "" && !confirm("Do you want to create a new account?")) {
        return;
    }

    stompClient.send("/app/account/login", {}, JSON.stringify({
        'uuid': uuid
    }));
}


function onLoginReceived(message) {
    if (message.status === "OK" || message.status === "CREATED") {
        if (message.content) {
            identityData = message.content;
            setCookie("_uuid", identityData.uuid, 365 * 5);
        }

        // Init Chat Connection
        chatSubscription = stompClient.subscribe('/topic/chat/' + identityData.highestCurrentLadder,
            (message) => handleChatUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        stompClient.subscribe('/user/queue/chat/',
            (message) => handleChatInit(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        initChat(identityData.highestCurrentLadder);

        // Init Ladder Connection
        ladderSubscription = stompClient.subscribe('/topic/ladder/' + identityData.highestCurrentLadder,
            (message) => handleLadderUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        stompClient.subscribe('/topic/global/',
            (message) => handleGlobalUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        stompClient.subscribe('/user/queue/ladder/',
            (message) => handleLadderInit(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        stompClient.subscribe('/user/queue/ladder/updates/',
            (message) => handlePrivateLadderUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
        initLadder(identityData.highestCurrentLadder);
    }
}

function promptNameChange() {
    let newUsername = window.prompt("What shall be your new name? (max. 32 characters)", ladderData.yourRanker.username);
    if (newUsername && newUsername.length > 32) {
        let temp = newUsername.substring(0, 32);
        alert('The maximum number of characters in your username is 32, not ' + newUsername.length + '!');
        newUsername = temp;
    }

    if (newUsername && newUsername.trim() !== "" && newUsername !== ladderData.yourRanker.username) {
        stompClient.send("/app/account/name", {}, JSON.stringify({
            'uuid': identityData.uuid,
            'content': newUsername
        }))
    }
}

function serializeClickEvent(event) {
    let serializableEvent = {
        altKey: event.altKey,
        bubbles: event.bubbles,
        button: event.button,
        buttons: event.buttons,
        cancelBubble: event.cancelBubble,
        cancelable: event.cancelable,
        clientX: event.clientX,
        clientY: event.clientY,
        composed: event.composed,
        ctrlKey: event.ctrlKey,
        defaultPrevented: event.defaultPrevented,
        delegateTarget: event.delegateTarget,
        detail: event.detail,
        eventPhase: event.eventPhase,
        isTrusted: event.isTrusted,
        layerX: event.layerX,
        layerY: event.layerY,
        metaKey: event.metaKey,
        movementX: event.movementX,
        movementY: event.movementY,
        mozInputSource: event.mozInputSource,
        mozPressure: event.mozPressure,
        offsetX: event.offsetX,
        offsetY: event.offsetY,
        pageX: event.pageX,
        pageY: event.pageY,
        rangeOffset: event.rangeOffset,
        region: event.region,
        relatedTarget: event.relatedTarget,
        returnValue: event.returnValue,
        screenX: event.screenX,
        screenY: event.screenY,
        shiftKey: event.shiftKey,
        timeStamp: event.timeStamp,
        type: event.type,
        which: event.which,
        x: event.x,
        y: event.y,
    }
    return JSON.stringify(serializableEvent);
}

function getUpgradeCost(level) {
    return new Decimal(Math.round(Math.pow(ladderData.currentLadder.number + 1, level)));
}

function getVinegarThrowCost() {
    return new Decimal(infoData.baseVinegarNeededToThrow.mul(new Decimal(ladderData.currentLadder.number)));
}

function getAutoPromoteGrapeCost(rank) {
    let minPeople = Math.max(infoData.minimumPeopleForPromote, ladderData.currentLadder.number);
    let divisor = Math.max(rank - minPeople + 1, 1);
    return infoData.baseGrapesNeededToAutoPromote.div(divisor).floor();
}

