<template>
  <div class="container px-3 py-1 message">
    <span
        v-for="part in messageParts"
        :key="part">
        <!-- Plain Message -->
        <span v-if="part.is(MessagePartType.plain)">
          {{ part.text }}
        </span>
        <!-- Mentions -->
        <span v-else-if="part.is(MessagePartType.mentionAtsign)" class="chat-mention-at">
            {{ part.text }}
        </span>
        <span v-else-if="part.is(MessagePartType.mentionName)" class="chat-mention">
            {{ part.text }}
        </span>
        <span v-else-if="part.is(MessagePartType.mentionNumber)" class="chat-mention-user-id">
            {{ part.text }}
        </span>
        <!-- Unimplemented -->
        <span v-else class="chat-unknown-part-type">
            {{ part.text }}
        </span>
      </span>
  </div>
</template>

<script setup>
import { defineProps } from "vue";

const props = defineProps({
  msg: Object,
});


//Basically an enum
const MessagePartType = {
    plain: Symbol("plain"),
    mentionName: Symbol("mentionName"),
    mentionNumber: Symbol("mentionNumber"),
    mentionAtsign: Symbol("mentionAtsign"),
}

class MessagePart {
    constructor(type, text) {
        this.type = type;
        this.text = text;
    }
    is(type) {
        return this.type === type;
    }
}

const messageParts = [
    new MessagePart(MessagePartType.plain, props.msg.message),
];

findMentions();


function spliceNewMessagePartsIntoArray(oldPart, newParts)
{
    messageParts.splice(messageParts.indexOf(oldPart), 1, ...newParts);
}

function findMentions() {
    //FIXME: This is demo data, we need to get the user list from the server
    let users = [{
        name: "John",
        id: "12345",
    }, {
        name: "Jane",
        id: "67890",
    }, {
        name: "Jane",
        id: "6",
    }];
    const sortedUsers = users.sort((a, b) => {
        const nameCompare = a.name.localeCompare(b.name);
        if (nameCompare !== 0) {
            return nameCompare;
        }
        return b.id.length -a.id.length;
    });

    sortedUsers.forEach(user => {
        findMention(user);
    });
}

function findMention(user) {
    for(let messagePart of messageParts) {
        if (messagePart.is(MessagePartType.plain)) {
            const mention = messagePart.text.indexOf(`@${user.name}#${user.id}`);
            if (mention !== -1) {
                const preMessagePart = new MessagePart(MessagePartType.plain, messagePart.text.substring(0, mention));
                const mentionAtsign = new MessagePart(MessagePartType.mentionAtsign, "@");
                const mentionName = new MessagePart(MessagePartType.mentionName, user.name);
                const mentionNumber = new MessagePart(MessagePartType.mentionNumber, '#'+user.id);
                const postMessagePart = new MessagePart(MessagePartType.plain, messagePart.text.substring(mention+`@${user.name}#${user.id}`.length));
                spliceNewMessagePartsIntoArray(messagePart, [preMessagePart, mentionAtsign, mentionName, mentionNumber, postMessagePart]);
                console.info(`Spliced ${messagePart.text} into ${preMessagePart.text},,${mentionAtsign.text},,${mentionName.text},,${mentionNumber.text},,${postMessagePart.text}`);
                findMentions(user);
            }
        }
    }
}


</script>

<style lang="scss" scoped>
@import "../../styles/styles";

.chat-mention-at,
.chat-mention {
  color: $main-color;
}

//This hurts my eyes and exactly that is what I want.
//Because this is shown when we forget to implement a part type
.chat-unknown-part-type {
    font-weight: bold;
    color: white;
    background-color: red;
}

.chat-mention-user-id {
  color: #cf573c;
}
</style>
