function setCookie(cname, cvalue, exdays) {
    const d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    let expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for(let i = 0; i <ca.length; i++) {
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

async function checkCookie() {
    let uuid = getCookie("_uuid");
    if (uuid !== "" && uuid !== null && uuid !== undefined) {
        await postJSON("/login", uuid, req => {
            if(req.status === 200 || req.status === 201){
                uuid = req.response.uuid
            }
            setCookie("_uuid", uuid, 365 * 5);
        });
    } else {
        getJSON("/register", req => {
            if (req.status === 201) {
                uuid = req.response.uuid;
                if (uuid !== "" && uuid != null) {
                    setCookie("_uuid", uuid, 365 * 5);
                }
            }
        });
    }
}