let rankerTemplate = {
    username: "",
    points: new Decimal(0),
    power: new Decimal(0),
    bias: 0,
    multiplier: 0,
    you: false,
    growing: true,
    timesAsshole: 0,
    grapes: new Decimal(0),          // only shows the actual Number on yourRanker
    vinegar: new Decimal(0)          // only shows the actual Number on yourRanker
}

let messageTemplate = {
    username: "Chad the Listener",
    message: "Sorry, I disabled this currently, hope you have a better time playing this way. https://discord.gg/Ud7UfFJmYj ",
    timesAsshole: 0
}

let ladderData = {
    rankers: [rankerTemplate],
    currentLadder: {number: 0, size: 1, growingRankerCount: 1},
    firstRanker: rankerTemplate,
    yourRanker: rankerTemplate,
    startRank: 1
};

let chatData = {
    messages: [messageTemplate],
    currentChatNumber: 1
}

let infoData = {
    updateLadderStepsBeforeSync: 30,
    updateChatStepsBeforeSync: 60,
    ladderAreaSize: 10,
    pointsForPromote: new Decimal(250000000),
    peopleForPromote: 10,
    assholeLadder: 15,
    assholeTags: [''],
    vinegarNeededToThrow: new Decimal(1000000)
}

let updateLadderSteps = 0;
let updateChatSteps = 0;

let biasButton;
let multiButton;

let numberFormatter;


async function setup() {
    numberFormatter = new numberformat.Formatter({
        format: 'hybrid',
        sigfigs: 15,
        flavor: 'short',
        minSuffix: 1e15,
        maxSmall: 0
    });

    await getInfo();

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

async function reloadLadder(forcedReload = false) {
    let size = ladderData.currentLadder.size;
    let rank = ladderData.yourRanker.rank;
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

    let biasCost = getUpgradeCost(ladderData.yourRanker.bias + 1);
    if (ladderData.yourRanker.points.cmp(biasCost) > 0) {
        biasButton.disabled = false;
    } else {
        biasButton.disabled = true;
    }
    let multiCost = getUpgradeCost(ladderData.yourRanker.multiplier + 1);
    if (ladderData.yourRanker.power.cmp(new Decimal(multiCost)) > 0) {
        multiButton.disabled = false;
    } else {
        multiButton.disabled = true;
    }
    $('#biasTooltip').attr('data-bs-original-title', numberFormatter.format(biasCost) + ' Points');
    $('#multiTooltip').attr('data-bs-original-title', numberFormatter.format(multiCost) + ' Power');

    let tag1 = '', tag2 = '';

    if (ladderData.yourRanker.vinegar.cmp(getVinegarThrowCost()) >= 0) {
        tag1 = '<p style="color: plum">'
        tag2 = '</p>'
    }

    $('#infoText').html('Sour Grapes: ' + numberFormatter.format(ladderData.yourRanker.grapes) + '<br>' + tag1 + 'Vinegar: ' + numberFormatter.format(ladderData.yourRanker.vinegar) + tag2);


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
    document.getElementById("usernameLink").innerHTML = ladderData.yourRanker.username;
    document.getElementById("usernameText").innerHTML =
        "+" + ladderData.yourRanker.bias + "   x" + ladderData.yourRanker.multiplier;

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
    let rank = (ranker.rank === 1 && !ranker.you && ranker.growing && ladderData.currentLadder.size >= infoData.peopleForPromote
        && ladderData.firstRanker.points.cmp(infoData.pointsForPromote) >= 0) ?
        '<a href="#" style="text-decoration: none" onclick="throwVinegar()">🍇</a>' : ranker.rank;
    row.insertCell(0).innerHTML = rank + assholeTag;
    row.insertCell(1).innerHTML = ranker.username;
    row.cells[1].style.overflow = "hidden";
    row.insertCell(2).innerHTML = numberFormatter.format(ranker.power) + ' [+' + ranker.bias + ' x' + ranker.multiplier + ']';
    row.cells[2].classList.add('text-end');
    row.insertCell(3).innerHTML = numberFormatter.format(ranker.points);
    row.cells[3].classList.add('text-end');
    if (ranker.you) row.classList.add('table-active');
    return row;
}

async function calculatePoints() {
    if (ladderData.yourRanker.rank === ladderData.currentLadder.size && ladderData.currentLadder.size >= infoData.peopleForPromote)
        ladderData.yourRanker.grapes = ladderData.yourRanker.grapes.add(new Decimal(1));
    ladderData.yourRanker.vinegar = ladderData.yourRanker.vinegar.add(ladderData.yourRanker.grapes);

    if (ladderData.startRank > 1 && ladderData.firstRanker.growing) {
        ladderData.firstRanker.points = ladderData.firstRanker.points.add(ladderData.firstRanker.power);
    }

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


    await sortLadder();
    await reloadLadder();
}

async function sortLadder(forcedReload = false) {
    ladderData.rankers.sort((a, b) => b.points.sub(a.points));

    for (let i = 0; i < ladderData.rankers.length; i++) {
        ladderData.rankers[i].rank = ladderData.startRank + i;
        if (ladderData.rankers[i].you) {
            ladderData.yourRanker.points = ladderData.rankers[i].points;
            ladderData.yourRanker.power = ladderData.rankers[i].power;
            ladderData.yourRanker.rank = ladderData.rankers[i].rank;
        }
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

function getUpgradeCost(level) {
    return Math.round(Math.pow(ladderData.currentLadder.number + 1, level));
}

function getVinegarThrowCost() {
    return infoData.vinegarNeededToThrow.mul(new Decimal(ladderData.currentLadder.number));
}




