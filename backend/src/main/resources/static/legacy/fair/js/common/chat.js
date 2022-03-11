let messageTemplate = {
    username: "Chad, the Listener",
    message: "Sorry, I'm currently resting my ears. If you want to be heard, head over into our Discord. https://discord.gg/ThKzCknfFr",
    timesAsshole: 0,
    accountId: 0,
    timeCreated: "00:00"
}

let chatData = {
    messages: [messageTemplate],
    currentChatNumber: 1
}


function initChat(ladderNum) {
    stompClient.send("/app/chat/init/" + ladderNum, {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function handleChatInit(message) {
    if (message.status === "OK") {
        if (message.content) {
            chatData = message.content;
        }
    }
    updateChat();
}

function sendMessage() {
    let messageInput = $('#messageInput')[0];
    let message = messageInput.value;
    if (message === "") return;
    messageInput.value = "";

    if (message && message.length > 280) {
        message = message.substring(0, 280);
    }

    stompClient.send("/app/chat/post/" + chatData.currentChatNumber, {}, JSON.stringify({
        'uuid': getCookie("_uuid"),
        'content': message
    }));
}

function handleChatUpdates(message) {
    if (message) {
        chatData.messages.unshift(message);
        if (chatData.messages.length > 30) chatData.messages.pop();
    }
    updateChat();
}

function changeChatRoom(ladderNum) {
    chatSubscription.unsubscribe();
    chatSubscription = stompClient.subscribe('/topic/chat/' + ladderNum,
        (message) => handleChatUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
    initChat(ladderNum);
}

function updateChat() {
    let body = $('#messagesBody')[0];
    body.innerHTML = "";
    for (let i = 0; i < chatData.messages.length; i++) {
        let message = chatData.messages[i];
        let row = body.insertRow();
        let assholeTag = (message.timesAsshole < infoData.assholeTags.length) ?
            infoData.assholeTags[message.timesAsshole] : infoData.assholeTags[infoData.assholeTags.length - 1];
        row.insertCell(0).innerHTML = "[" + message.timeCreated + "] " + assholeTag + " - " + message.username + ":";
        row.cells[0].classList.add('overflow-hidden')
        row.cells[0].style.whiteSpace = 'nowrap';
        row.insertCell(1).innerHTML = message.message;
    }
}

function updateChatUsername(event) {
    chatData.messages.forEach(message => {
        if (event.accountId === message.accountId) {
            message.username = event.data;
        }
    })
    updateChat();
}

function removeMessages(event) {
    chatData.messages = chatData.messages.filter((m) => {
        return event.accountId !== m.accountId
    })
    updateChat();
}