async function getInfo() {
    try {
        const response = await axios.get("/fair/info");
        if (response.status === 200 && response.data.pointsForPromote) {
            infoData = response.data;
            infoData.pointsForPromote = new Decimal(infoData.pointsForPromote);
            infoData.vinegarNeededToThrow = new Decimal(infoData.vinegarNeededToThrow);
        }
    } catch (err) {

    }
}

async function promptNameChange() {
    let newUsername = window.prompt("What shall be your new name? (max. 32 characters)", ladderData.yourRanker.username);
    if (newUsername && newUsername.length > 32) {
        let temp = newUsername.substring(0, 32);
        alert('The maximum number of characters in your username is 32, not ' + newUsername.length + '!');
        newUsername = temp;
    }

    if (newUsername && newUsername.trim() !== "" && newUsername !== ladderData.yourRanker.username) {
        try {
            const response = await axios.put('/fair/account', new URLSearchParams({
                username: newUsername
            }));
            if (response.status === 200) {
                updateLadderSteps = 0;
                updateChatSteps = 0;
                await getLadder();
                await getChat();
            }
        } catch (err) {
            if (err.response.status === 403) {
                return;
            }
            console.error(err);
        }
    }
}

async function getChat(ladderNum) {
    try {
        /*
        const response = await axios.get("/fair/chat?ladder=" + ladderNum);
        if (response.status === 200) {
            chatData = response.data;
        }
         */
    } catch (err) {

    }

    let body = $('#messagesBody')[0];
    body.innerHTML = "";
    for (let i = 0; i < chatData.messages.length; i++) {
        let message = chatData.messages[i];
        let row = body.insertRow();
        let assholeTag = (message.timesAsshole < infoData.assholeTags.length) ?
            infoData.assholeTags[message.timesAsshole] : infoData.assholeTags[infoData.assholeTags.length - 1];
        row.insertCell(0).innerHTML = message.username + ": " + assholeTag;
        row.cells[0].classList.add('overflow-hidden')
        row.cells[0].style.whiteSpace = 'nowrap';
        row.insertCell(1).innerHTML = "&nbsp;" + message.message;
    }
}

async function postChat() {
    let message = $('#messageInput')[0];
    const messageData = message.value;
    if (messageData === "") return;
    message.value = "";
    try {
        /*
        const response = await axios.post('/fair/chat', new URLSearchParams({
            ladder: chatData.currentChatNumber,
            message: messageData
        }));
        if (response.status === 200) {
            chatData = response.data;
        }
        updateChatSteps = 0;
        await getChat(chatData.currentChatNumber);
         */
    } catch (err) {
        // Resetting the value if he can't postChat
        message.value = messageData;
    }
}

async function buyBias() {
    biasButton.disabled = true;
    $('#biasTooltip').tooltip('hide');
    let cost = new Decimal(getUpgradeCost(ladderData.yourRanker.bias + 1));
    if (ladderData.yourRanker.points.compare(cost) > 0) {
        ladderData.yourRanker.points = 0;
        ladderData.yourRanker.bias += 1;
        try {
            const response = await axios.post('/fair/ranker/bias');
        } catch (err) {
            if (err.response.status === 403) {
                biasButton.disabled = false;
            }
        }
        updateLadderSteps = 0;
        await getLadder();
    }
}

async function buyMulti() {
    multiButton.disabled = true;
    $('#multiTooltip').tooltip('hide');
    let cost = new Decimal(getUpgradeCost(ladderData.yourRanker.multiplier + 1));
    if (ladderData.yourRanker.power.compare(cost) > 0) {
        ladderData.yourRanker.power = 0;
        ladderData.yourRanker.points = 0;
        ladderData.yourRanker.bias = 0;
        ladderData.yourRanker.multi += 1;
        try {
            const response = await axios.post('/fair/ranker/multiplier');
        } catch (err) {
            if (err.response.status === 403) {
                multiButton.disabled = false;
            }
        }
        updateLadderSteps = 0;
        await getLadder();
    }
}

async function getLadder(forcedReload = false) {
    try {
        const response = await axios.get("/fair/ranker");
        ladderData = response.data;
        ladderData.rankers.forEach(r => {
            r.points = new Decimal(r.points);
            r.power = new Decimal(r.power);
        });
        if (response.status === 200 && response.data.yourRanker) {
            ladderData.firstRanker.points = new Decimal(ladderData.firstRanker.points);
            ladderData.firstRanker.power = new Decimal(ladderData.firstRanker.power);
            ladderData.yourRanker.points = new Decimal(ladderData.yourRanker.points);
            ladderData.yourRanker.power = new Decimal(ladderData.yourRanker.power);
            ladderData.yourRanker.grapes = new Decimal(ladderData.yourRanker.grapes);
            ladderData.yourRanker.vinegar = new Decimal(ladderData.yourRanker.vinegar);
        }
        await sortLadder(forcedReload)
        await reloadLadder(forcedReload);
        reloadInformation();
    } catch (err) {
        console.error(err);
    }
}

async function promote() {
    $('#promoteButton').hide();
    try {
        const response = await axios.post('/fair/ranker/promote');
        if (response.status === 200) {
            updateLadderSteps = 0;
            await getLadder();
            updateChatSteps = 0;
            await getChat(chatData.currentChatNumber + 1);
        }
    } catch (err) {

    }
}

async function throwVinegar() {
    if (ladderData.yourRanker.vinegar.cmp(getVinegarThrowCost()) >= 0) {
        ladderData.yourRanker.vinegar = new Decimal(0);
        try {
            const response = await axios.post('fair/ranker/vinegar');
        } catch (err) {

        }
    }
}

async function beAsshole() {
    if (ladderData.firstRanker.you && ladderData.currentLadder.size >= infoData.peopleForPromote
        && ladderData.firstRanker.points.cmp(infoData.pointsForPromote) >= 0
        && ladderData.currentLadder.number === infoData.assholeLadder) {
        if (confirm("Do you really wanna be an Asshole?! This is your only chance, you can still cancel!")) {
            try {
                const response = await axios.post('fair/ranker/asshole');
                if (response.status === 200) {
                    updateLadderSteps = 0;
                    await getLadder();
                    updateChatSteps = 0;
                    await getChat(chatData.currentChatNumber + 1);
                }
            } catch (err) {
                return;
            }
        }
    }
}

