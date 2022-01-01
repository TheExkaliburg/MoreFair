let messageTemplate = {
    username: "name",
    message: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque et feugiat odio. Quisque vitae dolor finibus, tempor felis at, sagittis elit. Sed velit justo, rutrum et nibh sed, dignissim fringilla eros. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nam interdum nisl lorem, et sagittis libero."
}
let data = {
    messages: [messageTemplate, messageTemplate, messageTemplate]
}


async function getChat(ladderNum) {
    try {
        const response = await axios.get("/fair/chat", new URLSearchParams({
            uuid: getCookie("_uuid"),
            ladder: ladderNum
        }));
    } catch (err) {

    }
}