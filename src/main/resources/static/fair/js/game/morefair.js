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




