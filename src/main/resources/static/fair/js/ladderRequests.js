function initLadder(ladderNum) {
    stompClient.send("/app/ladder/init/" + ladderNum, {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function handleLadderInit(message) {
    if (message.status === "OK") {
        if (message.content) {
            ladderData = message.content;
        }
    }
    updateChat();
}

function handleLadderUpdates(message) {
    if (message) {
        console.log(message);
    }
    updateChat();
}

function changeLadder(ladderNum) {
    if (ladderSubscription) ladderSubscription.unsubscribe();
    ladderSubscription = stompClient.subscribe('/topic/chat/' + ladderNum,
        (message) => handleLadderUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
    initLadder(ladderNum);
}