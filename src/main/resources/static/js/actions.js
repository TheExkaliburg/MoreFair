let ladder = {};

function postJSON(url, data, onLoad = () => {}){
    let req = new XMLHttpRequest();
    req.open('POST', url);
    req.setRequestHeader('Content-Type', 'application/json');
    req.addEventListener("error", err => console.error(err.toString()))
    req.addEventListener("load", (req) => onLoad(req.target));
    req.send(JSON.stringify(data));
}

function getJSON(url, onLoad = () => {}){
    let req = new XMLHttpRequest();
    req.open("GET", url);
    req.setRequestHeader("Accept", "application/json");
    req.addEventListener("error", err => console.error(err.toString()));
    req.addEventListener("load", (req) => onLoad(req.target));
    req.responseType = "json";
    req.send();
}

function reloadLadder(data){
    ladder = data;
    let body = document.getElementById("ladderBody");
    body.innerHTML = "";
    data.forEach(item => {
        let row = body.insertRow();
        row.insertCell(0).innerHTML = item.position;
        row.insertCell(1).innerHTML = item.username;
        row.insertCell(2).innerHTML = item.points;
    })
}

function calculatePoints(){

}