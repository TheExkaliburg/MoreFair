let ladder = {data:[], fromPosition: 1};

function getLadder(){
    getJSON('/ladder', req => {
        ladder.data = req.response;
        reloadLadder();
    });
}

function reloadLadder(){
    let body = document.getElementById("ladderBody");
    body.innerHTML = "";
    ladder.data.forEach(item => {
        let row = body.insertRow();
        row.insertCell(0).innerHTML = item.rank;
        row.insertCell(1).innerHTML = item.username;
        row.insertCell(2).innerHTML = item.points;
    })
}

function calculatePoints(){
    ladder.data.forEach(user => {
        user.points += user.rank - 1;
    });

    ladder.data.sort((a,b) => b.points - a.points);

    for(let i = 0; i<ladder.data.length; i++){
        ladder.data[i].rank = ladder.fromPosition + i;
    }

    reloadLadder();
}