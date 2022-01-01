let rankerTemplate = {username: "", points: 0, power: 0, bias: 0, multiplier: 0, you: true}
let ladderData = {
    rankers: [rankerTemplate],
    currentLadder: {number: 0, size: 1, growingRankerCount: 1},
    firstRanker: rankerTemplate,
    startRank: 1
};
let yourRanker = rankerTemplate;

let messageTemplate = {
    username: "name",
    message: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque et feugiat odio. Quisque vitae dolor finibus, tempor felis at, sagittis elit. Sed velit justo, rutrum et nibh sed, dignissim fringilla eros. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nam interdum nisl lorem, et sagittis libero."
}
let chatData = {
    messages: [messageTemplate, messageTemplate, messageTemplate]
}


let updateSteps = 0;
const UPDATE_STEPS_BEFORE_SYNC = 10;
const SYNC_STEPS_BEFORE_CHAT = 3;
const LADDER_AREA_SIZE = 10;

let biasButton;
let multiButton

let numberFormatter;


async function setup() {
    numberFormatter = new numberformat.Formatter({
        format: 'hybrid',
        sigfigs: 15,
        flavor: 'short',
        minSuffix: 1e15,
        maxSmall: 0
    });

    biasButton = document.getElementById("biasButton");
    multiButton = document.getElementById("multiButton");

    await checkCookie();
    await getLadder();

    //biasButton.innerHTML = "+1 Bias<br>(2 Points)"
    biasButton.addEventListener("click", buyBias)

    //multiButton.innerHTML = "+1 Multi<br>(4 Power)"
    multiButton.addEventListener("click", buyMulti)

    window.setInterval(update, 1000);
}

async function update() {
    updateSteps++;
    if (updateSteps < UPDATE_STEPS_BEFORE_SYNC) {
        await calculatePoints();
    } else {
        await getLadder();
        updateSteps = 0;
    }
}

async function buyBias() {
    biasButton.disabled = true;
    $('#buyButtonGroup').tooltip('hide');
    let cost = getCost(yourRanker.bias + 1);
    if (yourRanker.points > cost) {
        try {
            const response = await axios.post('/fair/ranker/bias', new URLSearchParams({uuid: getCookie("_uuid")}));
            if (response.status === 200) {
                updateSteps = UPDATE_STEPS_BEFORE_SYNC;
                // await reloadLadder();
            }
        } catch (err) {
            if (err.response.status === 403) {
                biasButton.disabled = false;
                return;
            }
            console.error(err);
        }
    }
}


async function buyMulti() {
    multiButton.disabled = true;
    $('#buyButtonGroup').tooltip('hide');
    let cost = getCost(yourRanker.multiplier + 1);
    if (yourRanker.power > cost) {
        try {
            const response = await axios.post('/fair/ranker/multiplier', new URLSearchParams({uuid: getCookie("_uuid")}));
            if (response.status === 200) {
                updateSteps = UPDATE_STEPS_BEFORE_SYNC;
                // await reloadLadder();
            }
        } catch (err) {
            if (err.response.status === 403) {
                multiButton.disabled = false;
                return;
            }
            console.error(err);
        }
    }
}

async function getLadder(forcedReload = false) {
    try {
        const response = await axios.get("/fair/ranker?uuid=" + getCookie("_uuid"));
        ladderData = response.data;
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
    let startRank = rank + 1 - Math.round(LADDER_AREA_SIZE / 2);
    let endRank = rank + Math.round(LADDER_AREA_SIZE / 2) - 1;

    if (endRank >= size) {
        startRank = size + 2 - LADDER_AREA_SIZE;
        endRank = size;
    }

    if (startRank <= 1) {
        startRank = 1;
        endRank = Math.min(LADDER_AREA_SIZE, size);
    }

    if (!forcedReload && (ladderData.rankers[0].rank > startRank || ladderData.rankers[ladderData.rankers.length - 1].rank < endRank)) {
        await getLadder(true)
        updateSteps = 0;
        return;
    }

    let body = document.getElementById("ladderBody");
    body.innerHTML = "";
    if (startRank !== 1) writeNewRow(body, ladderData.firstRanker);
    for (let i = 0; i < ladderData.rankers.length; i++) {
        let ranker = ladderData.rankers[i];
        if ((ranker.rank >= startRank && ranker.rank <= endRank)) writeNewRow(body, ranker);
    }

    let biasCost = getCost(yourRanker.bias + 1);
    //biasButton.innerHTML = "+1 Bias<br>(" + numberFormatter.format(biasCost) + ")";
    if (yourRanker.points > biasCost) {
        biasButton.disabled = false;
    } else {
        biasButton.disabled = true;
    }
    let multiCost = getCost(yourRanker.multiplier + 1);
    //multiButton.innerHTML = "+1 Multi<br>(" + numberFormatter.format(multiCost) + ")";
    if (yourRanker.power > multiCost) {
        multiButton.disabled = false;
    } else {
        multiButton.disabled = true;
    }
    $('#buyButtonGroup').attr('data-original-title', 'Multi: ' + numberFormatter.format(multiCost) + "<br>Bias: " + numberFormatter.format(biasCost));
}

function reloadInformation() {
    let yourRanker = ladderData.rankers.filter(r => {
        return r.you === true;
    })[0];

    document.getElementById("usernameLink").innerHTML = yourRanker.username;
    document.getElementById("usernameText").innerHTML =
        "+" + yourRanker.bias + "   x" + yourRanker.multiplier;

    document.getElementById("rankerCount").innerHTML =
        "Active Rankers: " + ladderData.currentLadder.growingRankerCount + "/" + ladderData.currentLadder.size;
    document.getElementById("ladderNumber").innerHTML = "Ladder # " + ladderData.currentLadder.number;
}

function writeNewRow(body, ranker) {
    let row = body.insertRow();
    row.insertCell(0).innerHTML = ranker.rank;
    row.insertCell(1).innerHTML = ranker.username;
    row.insertCell(2).innerHTML = numberFormatter.format(ranker.power);
    row.cells[2].classList.add('text-end');
    row.insertCell(3).innerHTML = numberFormatter.format(ranker.points);
    row.cells[3].classList.add('text-end');
    if (ranker.you) row.classList.add('table-active');
}


// TODO: BREAK Infinity
async function calculatePoints() {
    ladderData.rankers.forEach(ranker => {
        ranker.power += ranker.rank !== 1 ? Math.round(((ranker.rank - 1) + ranker.bias) * ranker.multiplier) : 0;
        ranker.points += ranker.power;
    });

    if (ladderData.startRank > 1) {
        ladderData.firstRanker.points += ladderData.firstRanker.power;
    }

    await sortLadder()
    await reloadLadder();
}

async function sortLadder(forcedReload = false) {
    ladderData.rankers.sort((a, b) => b.points - a.points);

    for (let i = 0; i < ladderData.rankers.length; i++) {
        ladderData.rankers[i].rank = ladderData.startRank + i;
    }

    if (ladderData.rankers[0].rank === 1) {
        ladderData.firstRanker = ladderData.rankers[0];
    }

    // If my highest Ranker is higher than the firstRanker,
    // which shouldn't happen if we have the first Ranker in our rankerList,
    // then force reloading
    if (!forcedReload && ladderData.rankers[0].points > ladderData.firstRanker.points) {
        await getLadder(true)
        updateSteps = 0;
    }
}

function getCost(level) {
    return Math.round(Math.pow(ladderData.currentLadder.number + 1, level));
}


function format(number) {
    return numberFormatter.format(number);
}

async function promptNameChange() {
    let newUsername = window.prompt("What shall be your new name?", yourRanker.username);
    if (newUsername !== yourRanker.username && (newUsername)) {
        try {
            const response = await axios.put('/fair/account', new URLSearchParams({
                uuid: getCookie("_uuid"),
                username: newUsername
            }));
            if (response.status === 200) {
                updateSteps = UPDATE_STEPS_BEFORE_SYNC;
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
        const response = await axios.get("/fair/chat", new URLSearchParams({
            uuid: getCookie("_uuid"),
            ladder: ladderNum
        }));

        if (response.status === 200) {
            chatData = response.data;
        }

    } catch (err) {

    }
}