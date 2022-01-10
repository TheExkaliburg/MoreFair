function setCookie(cName, cValue, expDays) {
    const d = new Date();
    d.setTime(d.getTime() + (expDays * 24 * 60 * 60 * 1000));
    let expires = "expires=" + d.toUTCString();
    document.cookie = cName + "=" + cValue + ";" + expires + ";path=/";
}

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function login() {
    stompClient.send("/app/login", {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function onLoginReceived(message) {
    console.log(message);
    if (message.status === "OK" || message.status === "CREATED") {
        if (message.content.uuid) {
            let uuid = message.content.uuid;
            setCookie("_uuid", uuid, 365 * 5);
        }
    }

    // Init Chat Connection
    chatSubscription = stompClient.subscribe('/topic/chat/' + ladderData.currentLadder.number,
        (message) => handleChatUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
    stompClient.subscribe('/user/queue/chat/' + ladderData.currentLadder.number,
        (message) => handleChatInit(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
    initChat();
}

async function importCookie() {
    let newUUID = prompt("Paste your ID into here:");
    try {
        // Check if cookies are valid
        const response = await axios.post('/fair/login', new URLSearchParams({uuid: newUUID}));
        if (response.status === 200 && response.data.uuid) {
            let uuid = response.data.uuid;
            setCookie("_uuid", uuid, 365 * 5);
            // Relaod the page for the new cookies to take place
            location.reload();
        }

    } catch (err) {
        alert("Invalid ID!")
        console.error(err)
    }
}

async function exportCookie() {
    // Copy the text inside the text field
    await navigator.clipboard.writeText(getCookie("_uuid"));

    // Alert the copied text
    alert("Copied your ID to your clipboard! (don't lose it or give it away!)");
}