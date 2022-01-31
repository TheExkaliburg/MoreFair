let messageTemplate = {
    username: "Chad, the Listener",
    message: "Sorry, I'm currently resting my ears. If you want to be heard, head over into our Discord. https://discord.gg/Ud7UfFJmYj ",
    timesAsshole: 0,
    accountId: 0
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
    let reference_time = new Date().getTime()
    for (let i = 0; i < chatData.messages.length; i++) {
        let message = chatData.messages[i];
        let row = body.insertRow();
        let assholeTag = (message.timesAsshole < infoData.assholeTags.length) ?
            infoData.assholeTags[message.timesAsshole] : infoData.assholeTags[infoData.assholeTags.length - 1];
        row.insertCell(0).innerHTML = `[${fancyTime((reference_time-message.timestamp)/1000)}] ` + message.username + ": " + assholeTag;
        row.cells[0].classList.add('overflow-hidden')
        row.cells[0].style.whiteSpace = 'nowrap';
        row.insertCell(1).innerHTML = "&nbsp;" + message.message;
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

const time_names = [
		["second", "s"],
		["minute", "min"],
		["hour", "h"],
		["day", "d"],
		["week", "w"],
		["month", "m"],
		["year", "y"]
]
const time_modulos = [
		1,
		60,
		3600,
		3600*24,
		3600*24*7,
		3660*24*30,
		3660*24*365.25
]

function fancyTime(t) {
	let short = true
	let index = short ? 1 : 0;
    let time_values = []
	let slotted_time = 0
    // Iterates modulos in reverse
	// Adds fitting time to values
	// Subtracts t with added time
    for (let i = time_modulos.length; i > 0; i--) {
		slotted_time = Math.max(Math.floor(t/time_modulos[i-1]))
	    time_values.push(slotted_time)
		t = t - slotted_time*time_modulos[i-1]
    }
    console.log(time_values)
    // Connects name with index value
	// The value array is flipped upside down, so it requires some special care
	let pretty = []
	for (let i = time_names.length; i > 0; i--) {
	    console.log(time_names[i-1])
		if (time_values[time_values.length-i] > 0) {
			let _name = time_names[i-1][index]
			// Adds 's' to name if applicable. Example: 2 minute -> 2 minutes
			if ((time_values[time_values.length-i] > 1) && !(short)) {
				_name = _name + "s"
			}
			pretty.push(`${time_values[time_values.length-i]} ${_name}`)
		}
    }
	pretty = pretty.join(" ")
	return pretty	
}
