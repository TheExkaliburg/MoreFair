function postJSON(url, data, onLoad = () => {}){
    let req = new XMLHttpRequest();
    req.open('POST', url);
    req.setRequestHeader('Content-Type', 'application/json');
    req.addEventListener("error", err => console.error(err.toString()))
    req.addEventListener("load", (req) => onLoad(req.target));
    //req.responseType = "json";
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