let data = {
    rankers: [{username: "", points: 0, power: 0, bias: 0, multiplier: 0}],
    currentLadder: {number: 0}
};

async function setup() {
    await checkCookie();
    await getLadder();
    window.setInterval(calculatePoints, 1000);
    window.setInterval(getLadder, 10000);
}

async function getLadder() {
    try {
        const response = await axios.get("/fair/ranker?uuid=" + getCookie("_uuid"));
        data = response.data;
        sortLadder()
        reloadLadder();
    } catch (err) {
        console.error(err);
    }
}

function reloadLadder() {
    let body = document.getElementById("ladderBody");
    body.innerHTML = "";
    data.rankers.forEach(ranker => {
        let row = body.insertRow();
        row.insertCell(0).innerHTML = ranker.rank;
        row.insertCell(1).innerHTML = ranker.username;
        row.insertCell(2).innerHTML = "+" + ranker.bias + "/x" + ranker.multiplier;
        row.insertCell(3).innerHTML = ranker.points;
        row.insertCell(4).innerHTML = ranker.power;
    });
}

// TODO: BREAK Infinity
function calculatePoints() {
    data.rankers.forEach(ranker => {
        ranker.power += (ranker.rank - 1 + ranker.bias) * ranker.multiplier;
        ranker.points += ranker.power;
    });

    sortLadder()
    reloadLadder();
}

function sortLadder() {
    data.rankers.sort((a, b) => b.points - a.points);

    for (let i = 0; i < data.rankers.length; i++) {
        data.rankers[i].rank = data.currentLadder.number + i;
    }
}