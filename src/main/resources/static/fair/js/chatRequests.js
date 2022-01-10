function initChat() {
    stompClient.send("/app/initChat/" + chatData.currentChatNumber, {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function handleChatInit(message) {
    if (message.status === "OK") {
        if (message.content) {
            console.log(message);
            chatData = message.content;
        }
    }
    updateChat();
}

function postChat(message) {
    stompClient.send("/app/postChat/" + chatData.currentChatNumber, {}, JSON.stringify({
        'uuid': getCookie("_uuid"),
        'content': message
    }));
}

function handleChatUpdates(message) {
    if (message.status === "OK") {
        if (message.content) {
            console.log(message);
        }
    }
    updateChat();
}