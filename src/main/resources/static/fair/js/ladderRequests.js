function initLadder() {
    stompClient.send("/app/initChat/" + chatData.currentChatNumber, {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function handleLadderInit(message) {
    if (message.status === "OK") {
        if (message.content) {
            console.log(message);
            chatData = message.content;
        }
    }
    updateChat();
}

function handleChatUpdates(message) {
    if (message) {
        console.log(message);
        chatData.messages.unshift(message);
        if (chatData.messages.length > 30) chatData.messages.pop();
    }
    updateChat();
}