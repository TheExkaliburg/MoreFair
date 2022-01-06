let rankerTemplate = {
    username: "",
    points: new Decimal(0),
    power: new Decimal(0),
    bias: 0,
    multiplier: 0,
    you: false,
    growing: true,
    timesAsshole: 0
}
let ladderData = {
    rankers: [rankerTemplate],
    currentLadder: {number: 0, size: 1, growingRankerCount: 1},
    firstRanker: rankerTemplate,
    startRank: 1
};
let yourRanker = rankerTemplate;

let messageTemplate = {
    username: "Username",
    message: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque et feugiat odio.",
    timesAsshole: 1
}
let chatData = {
    messages: [messageTemplate, messageTemplate, messageTemplate],
    currentChatNumber: 1
}

let infoData = {
    updateLadderStepsBeforeSync: 10,
    updateChatStepsBeforeSync: 30,
    ladderAreaSize: 10,
    pointsForPromote: new Decimal(1000),
    peopleForPromote: 10,
    assholeLadder: 15,
    assholeTags: ['']
}

let updateLadderSteps = 0;
let updateChatSteps = 0;

let biasButton;
let multiButton;

let numberFormatter;


async function getInfo() {
    try {
        const response = await axios.get("/fair/info");
        if (response.status === 200) {
            infoData = response.data;
            infoData.pointsForPromote = new Decimal(infoData.pointsForPromote)
        }
    } catch (err) {

    }
}

async function setup() {
    numberFormatter = new numberformat.Formatter({
        format: 'hybrid',
        sigfigs: 15,
        flavor: 'short',
        minSuffix: 1e15,
        maxSmall: 0
    });

    getInfo();

    biasButton = $('#biasButton')[0];
    multiButton = $('#multiButton')[0];

    $('#messageInput')[0].addEventListener("keyup", event => {
        if (event.key === "Enter") {
            // Cancel the default action, if needed
            event.preventDefault();
            // Trigger the button element with a click
            postChat();
        }
    });

    await checkCookie();
    await getLadder();
    await getChat(ladderData.currentLadder.number);

    window.setInterval(update, 1000);
}

async function update() {
    updateLadderSteps++;
    if (updateLadderSteps < infoData.updateLadderStepsBeforeSync) {
        await calculatePoints();
    } else {
        await getLadder();
        updateLadderSteps = 0;
    }

    updateChatSteps++;
    if (updateChatSteps >= infoData.updateChatStepsBeforeSync) {
        await getChat(chatData.currentChatNumber);
        updateChatSteps = 0;
    }
}

async function buyBias() {
    biasButton.disabled = true;
    $('#biasTooltip').tooltip('hide');
    let cost = new Decimal(getCost(yourRanker.bias + 1));
    if (yourRanker.points.compare(cost) > 0) {
        yourRanker.points = 0;
        yourRanker.bias += 1;
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
    let cost = new Decimal(getCost(yourRanker.multiplier + 1));
    if (yourRanker.power.compare(cost) > 0) {
        yourRanker.power = 0;
        yourRanker.points = 0;
        yourRanker.bias = 0;
        yourRanker.multi += 1;
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
        ladderData.firstRanker.points = new Decimal(ladderData.firstRanker.points);
        ladderData.firstRanker.power = new Decimal(ladderData.firstRanker.power);

        yourRanker = ladderData.rankers.filter(r => {
            return r.you === true;
        })[0];

        await sortLadder(forcedReload)
        await reloadLadder(forcedReload);
        reloadInformation();
    } catch (err) {
        console.error(err);
    }
}

async function reloadLadder(forcedReload = false) {
    let size = ladderData.currentLadder.size;
    let rank = yourRanker.rank;
    let startRank = rank + 1 - Math.round(infoData.ladderAreaSize / 2);
    let endRank = rank + Math.round(infoData.ladderAreaSize / 2) - 1;

    if (endRank >= size) {
        startRank = size + 2 - infoData.ladderAreaSize;
        endRank = size;
    }

    if (startRank <= 1) {
        startRank = 1;
        endRank = Math.min(infoData.ladderAreaSize, size);
    }

    if (!forcedReload && (ladderData.rankers[0].rank > startRank || ladderData.rankers[ladderData.rankers.length - 1].rank < endRank)) {
        await getLadder(true)
        updateLadderSteps = 0;
        return;
    }

    let body = document.getElementById("ladderBody");
    body.innerHTML = "";
    if (startRank !== 1) writeNewRow(body, ladderData.firstRanker);
    for (let i = 0; i < ladderData.rankers.length; i++) {
        let ranker = ladderData.rankers[i];
        if ((ranker.rank >= startRank && ranker.rank <= endRank)) writeNewRow(body, ranker);
    }

    // if we dont have enough Ranker yet, fill the table with filler rows
    for (let i = body.rows.length; i < 10; i++) {
        writeNewRow(body, rankerTemplate);
        body.rows[i].style.visibility = 'hidden';
    }

    let biasCost = getCost(yourRanker.bias + 1);
    if (yourRanker.points.cmp(biasCost) > 0) {
        biasButton.disabled = false;
    } else {
        biasButton.disabled = true;
    }
    let multiCost = getCost(yourRanker.multiplier + 1);
    if (yourRanker.power.cmp(new Decimal(multiCost)) > 0) {
        multiButton.disabled = false;
    } else {
        multiButton.disabled = true;
    }
    $('#biasTooltip').attr('data-bs-original-title', numberFormatter.format(biasCost) + ' Points');
    $('#multiTooltip').attr('data-bs-original-title', numberFormatter.format(multiCost) + ' Power');
    showPromote();
}

function showPromote() {
    let promoteButton = $('#promoteButton');
    let assholeButton = $('#assholeButton');
    let ladderNumber = $('#ladderNumber');

    if (ladderData.firstRanker.you && ladderData.currentLadder.size >= infoData.peopleForPromote && ladderData.firstRanker.points.cmp(infoData.pointsForPromote) >= 0) {
        if (ladderData.currentLadder.number === infoData.assholeLadder) {
            promoteButton.hide()
            ladderNumber.hide()
            assholeButton.show()
        } else {
            assholeButton.hide()
            ladderNumber.hide()
            promoteButton.show()
        }
    } else {
        assholeButton.hide()
        promoteButton.hide()
        ladderNumber.show()
    }
}

function reloadInformation() {
    let yourRanker = ladderData.rankers.filter(r => {
        return r.you === true;
    })[0];

    document.getElementById("usernameLink").innerHTML = yourRanker.username;
    document.getElementById("usernameText").innerHTML =
        "+" + yourRanker.bias + "   x" + yourRanker.multiplier;

    document.getElementById("rankerCount").innerHTML =
        "Rankers: " + ladderData.currentLadder.growingRankerCount + "/" + ladderData.currentLadder.size;
    document.getElementById("ladderNumber").innerHTML = "Ladder # " + ladderData.currentLadder.number;

    let offCanvasBody = $('#offCanvasBody');
    offCanvasBody.empty();
    for (let i = 1; i <= ladderData.currentLadder.number; i++) {
        let ladder = $(document.createElement('li')).prop({
            class: "nav-link"
        });

        let ladderLinK = $(document.createElement('a')).prop({
            href: '#',
            innerHTML: 'Chad #' + i,
            class: "nav-link h5"
        });

        ladderLinK.click(async function () {
            await getChat(i);
        })

        ladder.append(ladderLinK);
        offCanvasBody.prepend(ladder);
    }
}

function writeNewRow(body, ranker) {
    let row = body.insertRow();

    let assholeTag = (ranker.timesAsshole < infoData.assholeTags.length) ?
        infoData.assholeTags[ranker.timesAsshole] : infoData.assholeTags[infoData.assholeTags.length - 1];
    row.insertCell(0).innerHTML = ranker.rank + assholeTag;
    row.insertCell(1).innerHTML = ranker.username;
    row.cells[1].style.overflow = "hidden";
    row.insertCell(2).innerHTML = numberFormatter.format(ranker.power);
    row.cells[2].classList.add('text-end');
    row.insertCell(3).innerHTML = numberFormatter.format(ranker.points);
    row.cells[3].classList.add('text-end');
    if (ranker.you) row.classList.add('table-active');
}

async function calculatePoints() {
    ladderData.rankers.forEach(ranker => {
        if (ranker.growing) {
            let temp = new Decimal(0);
            if (ranker.rank !== 1) {
                temp = new Decimal(ranker.rank - 1 + ranker.bias);
                temp = temp.multiply(new Decimal(ranker.multiplier));
            }

            ranker.power = ranker.power.add(temp);
            ranker.points = ranker.points.add(ranker.power);
        }
    });

    if (ladderData.startRank > 1) {
        ladderData.firstRanker.points = ladderData.firstRanker.points.add(ladderData.firstRanker.power);
    }

    await sortLadder()
    await reloadLadder();
}

async function sortLadder(forcedReload = false) {
    ladderData.rankers.sort((a, b) => b.points.sub(a.points));

    for (let i = 0; i < ladderData.rankers.length; i++) {
        ladderData.rankers[i].rank = ladderData.startRank + i;
    }

    if (ladderData.rankers[0].rank === 1) {
        ladderData.firstRanker = ladderData.rankers[0];
    }

    // If my highest Ranker is higher than the firstRanker,
    // which shouldn't happen if we have the first Ranker in our rankerList,
    // then force reloading
    if (!forcedReload && ladderData.rankers[0].points.compare(ladderData.firstRanker.points) > 0) {
        await getLadder(true)
        updateLadderSteps = 0;
    }
}

function getCost(level) {
    return Math.round(Math.pow(ladderData.currentLadder.number + 1, level));
}


function format(number) {
    return numberFormatter.format(number);
}

async function promptNameChange() {
    let newUsername = window.prompt("What shall be your new name? (max. 32 characters)", yourRanker.username);
    if (newUsername && newUsername.length > 32) {
        let temp = newUsername.substring(0, 32);
        alert('The maximum number of characters in your username is 32, not ' + newUsername.length + '!');
        newUsername = temp;
    }

    if (newUsername && newUsername.trim() !== "" && newUsername !== yourRanker.username) {
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
        const response = await axios.get("/fair/chat?ladder=" + ladderNum);
        if (response.status === 200) {
            chatData = response.data;
        }
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
        row.insertCell(1).innerHTML = message.message;
    }

}

async function postChat() {
    let message = $('#messageInput')[0];
    const messageData = message.value;
    if (messageData === "") return;
    message.value = "";
    try {
        const response = await axios.post('/fair/chat', new URLSearchParams({
            ladder: chatData.currentChatNumber,
            message: messageData
        }));
        if (response.status === 200) {
            chatData = response.data;
        }
        updateChatSteps = 0;
        await getChat(chatData.currentChatNumber);
    } catch (err) {
        // Resetting the value if he can't postChat
        message.value = messageData;
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