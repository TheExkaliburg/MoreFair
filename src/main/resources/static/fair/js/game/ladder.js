let rankerTemplate = {
    accountId: 0,
    username: "",
    rank: 0,
    points: new Decimal(0),
    power: new Decimal(0),
    bias: 0,
    multiplier: 0,
    you: false,
    growing: true,
    timesAsshole: 0,
    grapes: new Decimal(0),          // only shows the actual Number on yourRanker
    vinegar: new Decimal(0),         // only shows the actual Number on yourRanker
    autoPromote: false
}


let ladderData = {
    rankers: [rankerTemplate],
    currentLadder: {number: 1},
    firstRanker: rankerTemplate,
    yourRanker: rankerTemplate
};

let ladderStats = {
    growingRankerCount: 0,
    pointsNeededForManualPromote: new Decimal(0),
    eta: 0
}

function initLadder(ladderNum) {
    stompClient.send("/app/ladder/init/" + ladderNum, {}, JSON.stringify({
        'uuid': getCookie("_uuid")
    }));
}

function handleLadderInit(message) {
    if (message.status === "OK") {
        if (message.content) {
            ladderData = message.content;
            ladderData.rankers.forEach(ranker => {
                ranker.power = new Decimal(ranker.power);
                ranker.points = new Decimal(ranker.points);

                if (ranker.you) {
                    ranker.grapes = new Decimal(ranker.grapes);
                    ranker.vinegar = new Decimal(ranker.vinegar);
                } else {
                    ranker.grapes = new Decimal(0);
                    ranker.vinegar = new Decimal(0);
                }
            })

            if (!ladderData.yourRanker) ladderData.yourRanker = rankerTemplate;

            ladderData.yourRanker.power = new Decimal(ladderData.yourRanker.power);
            ladderData.yourRanker.points = new Decimal(ladderData.yourRanker.points);
            ladderData.yourRanker.grapes = new Decimal(ladderData.yourRanker.grapes);
            ladderData.yourRanker.vinegar = new Decimal(ladderData.yourRanker.vinegar);

            ladderData.firstRanker.power = new Decimal(ladderData.firstRanker.power);
            ladderData.firstRanker.points = new Decimal(ladderData.firstRanker.points);
            ladderData.firstRanker.grapes = new Decimal(0);
            ladderData.firstRanker.vinegar = new Decimal(0);
        }
    }
    updateLadder();
}

function buyBias(event) {
    let cost = new Decimal(getUpgradeCost(ladderData.yourRanker.bias + 1));
    let biasButton = $('#biasButton');
    let biasTooltip = $('#biasTooltip');

    if (ladderData.yourRanker.points.cmp(Decimal.max(ladderData.firstRanker.points, infoData.pointsForPromote.mul(ladderData.currentLadder.number)).mul(0.8)) >= 0) {
        if (!confirm("You're really close to the top, are you sure, you want to bias.")) {
            biasButton.prop("disabled", true);
            biasTooltip.tooltip('hide');
            return;
        }
    }

    biasButton.prop("disabled", true);
    biasTooltip.tooltip('hide');
    if (ladderData.yourRanker.points.compare(cost) > 0) {
        stompClient.send("/app/ladder/post/bias", {}, JSON.stringify({
            'uuid': identityData.uuid,
            'event': serializeClickEvent(event)
        }));
    }
}

function buyMulti(event) {
    let cost = getUpgradeCost(ladderData.yourRanker.multiplier + 1);
    let multiButton = $('#multiButton');
    let multiTooltip = $('#multiTooltip');

    if (ladderData.yourRanker.points.cmp(Decimal.max(ladderData.firstRanker.points, infoData.pointsForPromote.mul(ladderData.currentLadder.number)).mul(0.8)) >= 0) {
        if (!confirm("You're really close to the top, are you sure, you want to multi.")) {
            multiButton.prop("disabled", true);
            multiTooltip.tooltip('hide');
            return;
        }
    }

    multiButton.prop("disabled", true);
    multiTooltip.tooltip('hide');
    if (ladderData.yourRanker.power.compare(cost) > 0) {
        stompClient.send("/app/ladder/post/multi", {}, JSON.stringify({
            'uuid': identityData.uuid,
            'event': serializeClickEvent(event)
        }));
    }
}

function throwVinegar(event) {
    if (ladderData.yourRanker.vinegar.cmp(getVinegarThrowCost()) >= 0) {
        stompClient.send("/app/ladder/post/vinegar", {}, JSON.stringify({
            'uuid': identityData.uuid,
            'event': serializeClickEvent(event)
        }));
    }
}

function promote(event) {
    $('#promoteButton').hide();
    stompClient.send("/app/ladder/post/promote", {}, JSON.stringify({
        'uuid': identityData.uuid,
        'event': serializeClickEvent(event)
    }));
}

function beAsshole(event) {
    if (ladderData.firstRanker.you && ladderData.rankers.length >= Math.max(infoData.minimumPeopleForPromote, ladderData.currentLadder.number)
        && ladderData.firstRanker.points.cmp(infoData.pointsForPromote.mul(ladderData.currentLadder.number)) >= 0
        && ladderData.currentLadder.number >= infoData.assholeLadder) {
        if (confirm("Do you really wanna be an Asshole?!")) {
            stompClient.send("/app/ladder/post/asshole", {}, JSON.stringify({
                'uuid': identityData.uuid,
                'event': serializeClickEvent(event)
            }));
        }
    }
}

function buyAutoPromote(event) {
    $('#biasButton').prop("disabled", true);
    $('#autoPromoteTooltip').tooltip('hide');
    if (ladderData.currentLadder.number >= infoData.autoPromoteLadder
        && ladderData.currentLadder.number !== infoData.assholeLadder
        && ladderData.yourRanker.grapes.cmp(getAutoPromoteGrapeCost(ladderData.yourRanker.rank)) >= 0) {
        stompClient.send("/app/ladder/post/auto-promote", {}, JSON.stringify({
            'uuid': identityData.uuid,
            'event': serializeClickEvent(event)
        }));

    }
}

function handleLadderUpdates(message) {
    if (message) {
        message.events.forEach(e => handleEvent(e))
    }
    calculateLadder(message.secondsPassed);
    updateLadder();
}

function handleGlobalUpdates(message) {
    if (message) {
        message.forEach(e => handleEvent(e))
    }
}


function handlePrivateLadderUpdates(message) {
    if (message) {
        message.events.forEach(e => handleEvent(e))
    }
    updateLadder();
}

function changeLadder(ladderNum) {
    if (ladderSubscription) ladderSubscription.unsubscribe();
    ladderSubscription = stompClient.subscribe('/topic/ladder/' + ladderNum,
        (message) => handleLadderUpdates(JSON.parse(message.body)), {uuid: getCookie("_uuid")});
    initLadder(ladderNum);
}


function handleEvent(event) {
    switch (event.eventType) {
        case 'BIAS':
            handleBias(event);
            break;
        case 'MULTI':
            handleMultiplier(event);
            break;
        case 'VINEGAR':
            handleVinegar(event);
            break;
        case 'SOFT_RESET_POINTS':
            handleSoftResetPoints(event);
            break;
        case 'PROMOTE':
            handlePromote(event);
            break;
        case 'AUTO_PROMOTE':
            handleAutoPromote(event);
            break;
        case 'JOIN':
            handleJoin(event);
            break;
        case 'NAME_CHANGE':
            handleNameChange(event);
            break;
        case 'CONFIRM':
            handleConfirm(event);
            break;
        case 'RESET':
            handleReset(event);
            break;
        case 'BAN':
        case 'MUTE':
            removeMessages(event);
            break;
    }
}

function handleBias(event) {
    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId) {
            ranker.bias += 1;
            ranker.points = new Decimal(0);
        }
    });
}

function handleMultiplier(event) {
    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId) {
            ranker.multiplier += 1;
            ranker.bias = 0;
            ranker.points = new Decimal(0);
            ranker.power = new Decimal(0);
        }
    });
}

function handleVinegar(event) {
    let vinegarThrown = new Decimal(event.data.amount);

    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId) {
            if (ranker.you) ranker.vinegar = new Decimal(0);

            if (ladderData.firstRanker.you && event.data.success) {
                setTimeout(function () {
                    alert(ranker.username + " threw their " + numberFormatter.format(vinegarThrown) + " Vinegar at you and made you slip to the bottom of the ladder.");
                }, 1);

            }
        }
    });

    ladderData.rankers[0].vinegar = Decimal.max(ladderData.rankers[0].vinegar.sub(vinegarThrown), 0);
}

function handleSoftResetPoints(event) {
    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId) {
            ranker.points = new Decimal(0);
        }
    });
}

function handlePromote(event) {
    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId) {
            ranker.growing = false;
        }
    });

    if (event.accountId === identityData.accountId) {
        let newLadderNum = ladderData.currentLadder.number + 1
        changeLadder(newLadderNum);
        changeChatRoom(newLadderNum);
    }
}

function handleAutoPromote(event) {
    ladderData.rankers.forEach(ranker => {
        if (ranker.you && event.accountId === ranker.accountId) {
            ranker.grapes = ranker.grapes.sub(getAutoPromoteGrapeCost(ranker.rank));
            ranker.autoPromote = true;
        }
    })
}

function handleJoin(event) {
    let newRanker = {
        accountId: event.accountId,
        username: event.data.username,
        points: new Decimal(0),
        power: new Decimal(1),
        bias: 0,
        multiplier: 1,
        you: false,
        growing: true,
        timesAsshole: event.data.timesAsshole,
        grapes: new Decimal(0),          // only shows the actual Number on yourRanker
        vinegar: new Decimal(0)          // only shows the actual Number on yourRanker
    }

    if (!ladderData.rankers.find(r => r.accountId === event.accountId)) {
        if (newRanker.accountId !== identityData.accountId)
            ladderData.rankers.push(newRanker);
    }
}

function handleNameChange(event) {
    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId) {
            ranker.username = event.data;
        }
    })
    updateChatUsername(event);
}

function handleConfirm(event) {
    ladderData.rankers.forEach(ranker => {
        if (event.accountId === ranker.accountId && ranker.you) {
            confirm(event.data);
        }
    })
}

async function handleReset(event) {
    disconnect();
    await new Promise(r => setTimeout(r, 65000));
    location.reload();
}

function calculateLadder(delta) {
    ladderData.rankers = ladderData.rankers.sort((a, b) => b.points.sub(a.points));
    ladderStats.growingRankerCount = 0;
    for (let i = 0; i < ladderData.rankers.length; i++) {
        ladderData.rankers[i].rank = i + 1;
        // If the ranker is currently still on ladder
        if (ladderData.rankers[i].growing) {
            ladderStats.growingRankerCount += 1;
            // Calculating Points & Power
            if (ladderData.rankers[i].rank !== 1)
                ladderData.rankers[i].power = ladderData.rankers[i].power.add(
                    new Decimal((ladderData.rankers[i].bias + ladderData.rankers[i].rank - 1) * ladderData.rankers[i].multiplier)
                        .mul(new Decimal(delta)).floor());
            ladderData.rankers[i].points = ladderData.rankers[i].points.add(ladderData.rankers[i].power.mul(delta).floor());

            // Calculating Vinegar based on Grapes count
            if (ladderData.rankers[i].rank !== 1 && ladderData.rankers[i].you)
                ladderData.rankers[i].vinegar = ladderData.rankers[i].vinegar.add(ladderData.rankers[i].grapes.mul(delta).floor());

            if (ladderData.rankers[i].rank === 1 && ladderData.rankers[i].you && isLadderUnlocked())
                ladderData.rankers[i].vinegar = ladderData.rankers[i].vinegar.mul(Math.pow(0.9975, delta)).floor();

            for (let j = i - 1; j >= 0; j--) {
                // If one of the already calculated Rankers have less points than this ranker
                // swap these in the list... This way we keep the list sorted, theoretically
                let currentRanker = ladderData.rankers[j + 1];
                if (currentRanker.points.cmp(ladderData.rankers[j].points) > 0) {
                    // Move 1 Position up and move the ranker there 1 Position down

                    // Move other Ranker 1 Place down
                    ladderData.rankers[j].rank = j + 2;
                    if (ladderData.rankers[j].growing && ladderData.rankers[j].you && ladderData.rankers[j].multiplier > 1)
                        ladderData.rankers[j].grapes = ladderData.rankers[j].grapes.add(new Decimal(1));
                    ladderData.rankers[j + 1] = ladderData.rankers[j];

                    // Move this Ranker 1 Place up
                    currentRanker.rank = j + 1;
                    ladderData.rankers[j] = currentRanker;
                } else {
                    break;
                }
            }
        }
    }

    // Ranker on Last Place gains 1 Grape, only if he isn't the only one
    if (ladderData.rankers.length >= Math.max(infoData.minimumPeopleForPromote, ladderData.currentLadder.number)) {
        let index = ladderData.rankers.length - 1;
        if (ladderData.rankers[index].growing && ladderData.rankers[index].you)
            ladderData.rankers[index].grapes = ladderData.rankers[index].grapes.add(new Decimal(3).mul(delta).floor());
    }

    // Set yourRanker and firstRanker
    ladderData.rankers.forEach(ranker => {
        if (ranker.you) {
            ladderData.yourRanker = ranker;
        }
    })
    ladderData.firstRanker = ladderData.rankers[0];

    calculateStats();
}

function calculateStats() {
    // Points Needed For Promotion
    if (ladderData.rankers.length >= Math.max(infoData.minimumPeopleForPromote, ladderData.currentLadder.number)) {
        if (ladderData.firstRanker.points.cmp(infoData.pointsForPromote.mul(ladderData.currentLadder.number)) >= 0) {
            if (ladderData.currentLadder.number >= infoData.autoPromoteLadder) {
                let leadingRanker = ladderData.firstRanker.you ? ladderData.yourRanker : ladderData.firstRanker;
                let pursuingRanker = ladderData.firstRanker.you ? ladderData.rankers[1] : ladderData.yourRanker;

                // How many more points does the ranker gain against his pursuer, every Second
                let powerDiff = (leadingRanker.growing ? leadingRanker.power : new Decimal(0)).sub(pursuingRanker.growing ? pursuingRanker.power : 0);
                // Calculate the needed Point difference, to have f.e. 30seconds of point generation with the difference in power
                let neededPointDiff = powerDiff.mul(infoData.manualPromoteWaitTime).abs();

                ladderStats.pointsNeededForManualPromote = Decimal.max((leadingRanker.you ? pursuingRanker : leadingRanker)
                    .points.add(neededPointDiff), infoData.pointsForPromote.mul(ladderData.currentLadder.number));
            } else {
                ladderStats.pointsNeededForManualPromote = ladderData.firstRanker.you ? ladderData.rankers[1].points.add(1) : ladderData.firstRanker.points.add(1);
            }
        } else {
            ladderStats.pointsNeededForManualPromote = infoData.pointsForPromote.mul(ladderData.currentLadder.number);
        }

    } else {
        ladderStats.pointsNeededForManualPromote = new Decimal(Infinity);
    }

    // ETA
    // TODO: ETA
}

function updateLadder() {
    let size = ladderData.rankers.length;
    let rank = ladderData.yourRanker.rank;
    let ladderArea = Math.floor(rank / clientData.ladderAreaSize);
    let ladderAreaIndex = ladderArea * clientData.ladderAreaSize + 1;

    let startRank = ladderAreaIndex - clientData.ladderPadding;
    let endRank = ladderAreaIndex + clientData.ladderAreaSize + clientData.ladderPadding - 1;
    // If at start of the ladder
    if (startRank < 1) {
        endRank -= startRank - 1
    }
    // If at end of the ladder
    if (endRank > size) {
        startRank -= endRank - size;
    }

    let body = document.getElementById("ladderBody");
    body.innerHTML = "";
    for (let i = 0; i < ladderData.rankers.length; i++) {
        let ranker = ladderData.rankers[i];
        if (ranker.rank === startRank) writeNewRow(body, ladderData.firstRanker);
        if ((ranker.rank > startRank && ranker.rank <= endRank)) writeNewRow(body, ranker);
    }

    // if we dont have enough Ranker yet, fill the table with filler rows
    for (let i = body.rows.length; i < clientData.ladderAreaSize + clientData.ladderPadding * 2; i++) {
        writeNewRow(body, rankerTemplate);
        body.rows[i].style.visibility = 'hidden';
    }

    let tag1 = '', tag2 = '';
    if (ladderData.yourRanker.vinegar.cmp(getVinegarThrowCost()) >= 0) {
        tag1 = '<p style="color: plum">'
        tag2 = '</p>'
    }

    $('#infoText').html('Sour Grapes: ' + numberFormatter.format(ladderData.yourRanker.grapes) + '<br>' + tag1 + 'Vinegar: ' + numberFormatter.format(ladderData.yourRanker.vinegar) + tag2);

    $('#usernameLink').html(ladderData.yourRanker.username);

    $('#rankerCount').html("Rankers: " + ladderStats.growingRankerCount + "/" + ladderData.rankers.length);
    $('#ladderNumber').html("Ladder # " + ladderData.currentLadder.number);

    $('#manualPromoteText').html("Points needed for "
        + ((ladderData.currentLadder.number === infoData.assholeLadder) ? "being an asshole" : "manually promoting")
        + ": " + numberFormatter.format(ladderStats.pointsNeededForManualPromote));

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
            changeChatRoom(i);
        })

        ladder.append(ladderLinK);
        offCanvasBody.prepend(ladder);
    }

    showButtons();
}

function writeNewRow(body, ranker) {
    let row = body.insertRow();
    if (!ranker.growing) row.classList.add('strikeout')
    let assholeTag = (ranker.timesAsshole < infoData.assholeTags.length) ?
        infoData.assholeTags[ranker.timesAsshole] : infoData.assholeTags[infoData.assholeTags.length - 1];
    let rank = (ranker.rank === 1 && !ranker.you && ranker.growing && ladderData.rankers.length >= Math.max(infoData.minimumPeopleForPromote, ladderData.currentLadder.number)
        && ladderData.firstRanker.points.cmp(infoData.pointsForPromote.mul(ladderData.currentLadder.number)) >= 0 && ladderData.yourRanker.vinegar.cmp(getVinegarThrowCost()) >= 0) ?
        '<a href="#" style="text-decoration: none" onclick="throwVinegar(event)">🍇</a>' : ranker.rank;
    row.insertCell(0).innerHTML = rank + " " + assholeTag;
    row.insertCell(1).innerHTML = ranker.username;
    row.cells[1].style.overflow = "hidden";
    row.insertCell(2).innerHTML = numberFormatter.format(ranker.power) +
        ' [+' + ('' + ranker.bias).padStart(2, '0') + ' x' + ('' + ranker.multiplier).padStart(2, '0') + ']';
    row.cells[2].classList.add('text-end');
    row.insertCell(3).innerHTML = numberFormatter.format(ranker.points);
    row.cells[3].classList.add('text-end');
    if (ranker.you) row.classList.add('table-active');
    return row;
}

function showButtons() {
    // Bias and Multi Button Logic
    let biasButton = $('#biasButton');
    let multiButton = $('#multiButton');

    let biasCost = getUpgradeCost(ladderData.yourRanker.bias + 1);
    if (ladderData.yourRanker.points.cmp(biasCost) >= 0) {
        biasButton.prop("disabled", false);
    } else {
        biasButton.prop("disabled", true);
    }

    let multiCost = getUpgradeCost(ladderData.yourRanker.multiplier + 1);
    if (ladderData.yourRanker.power.cmp(new Decimal(multiCost)) >= 0) {
        multiButton.prop("disabled", false);
    } else {
        multiButton.prop("disabled", true);
    }
    $('#biasTooltip').attr('data-bs-original-title', numberFormatter.format(biasCost) + ' Points');
    $('#multiTooltip').attr('data-bs-original-title', numberFormatter.format(multiCost) + ' Power');

    // Promote and Asshole Button Logic
    let promoteButton = $('#promoteButton');
    let assholeButton = $('#assholeButton');
    let ladderNumber = $('#ladderNumber');

    if (ladderData.firstRanker.you && ladderData.firstRanker.points.cmp(ladderStats.pointsNeededForManualPromote) >= 0) {
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

    // Auto-Promote Button
    let autoPromoteButton = $('#autoPromoteButton');
    let autoPromoteTooltip = $('#autoPromoteTooltip');
    let autoPromoteCost = getAutoPromoteGrapeCost(ladderData.yourRanker.rank);
    if (!ladderData.yourRanker.autoPromote && ladderData.currentLadder.number >= infoData.autoPromoteLadder
        && ladderData.currentLadder.number !== infoData.assholeLadder) {

        autoPromoteButton.show();
        if (ladderData.yourRanker.grapes.cmp(autoPromoteCost) >= 0) {
            autoPromoteButton.prop("disabled", false);
        } else {
            autoPromoteButton.prop("disabled", true);
        }
        autoPromoteTooltip.attr('data-bs-original-title', numberFormatter.format(autoPromoteCost) + ' Grapes');
    } else {
        autoPromoteButton.hide();
    }


}

function isLadderUnlocked() {
    if (ladderData.rankers.length <= 0) return false;
    let rankerCount = ladderData.rankers.length;
    if (rankerCount < getRequiredRankerCountToUnlockLadder()) return false;
    return ladderData.firstRanker.points.cmp(getRequiredPointsToUnlockLadder()) >= 0;
}


function getRequiredRankerCountToUnlockLadder() {
    return Math.max(infoData.minimumPeopleForPromote, ladderData.currentLadder.number);
}

function getRequiredPointsToUnlockLadder() {
    return infoData.pointsForPromote.mul(ladderData.currentLadder.number);
}