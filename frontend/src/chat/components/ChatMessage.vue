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

processAllMentions();


function spliceNewMessagePartsIntoArray(oldPart, newParts)
{
    messageParts.splice(messageParts.indexOf(oldPart), 1, ...newParts);
}

function processAllMentions()
{
    //deep copy the array
    const messagePartsCopy = [...messageParts];
    messagePartsCopy.forEach(part => {
        if (part.is(MessagePartType.plain)) {
            const newParts = findMentions(part);
            spliceNewMessagePartsIntoArray(part, newParts);
        }
    });
}

function findMentions(messagePart) {
    const messageParts = [];
    //keep track of where we are in the message
    let currentIndex = 0;
    const message = messagePart.text;
    //find the first @
    let mentionIndex = message.indexOf("@");
    outer:
    while (mentionIndex !== -1) {
        let endOfMentionIndex = mentionIndex + 1;
        //find the first #
        while (message[endOfMentionIndex] !== "#") {
            endOfMentionIndex++;
            if(endOfMentionIndex >= message.length) {
                break outer;
            }
        }
        //check if the next character is a number
        if (isNaN(message[endOfMentionIndex + 1])) {
            //if it isn't, we have a bad mention
            continue;
        }
        endOfMentionIndex++;
        //look for the last number in the mention
        while (!isNaN(message[endOfMentionIndex])) {
            endOfMentionIndex++;
            if(endOfMentionIndex >= message.length) {
                break;
            }
        }

        const possibleMention = message.substring(mentionIndex, endOfMentionIndex);

        //TODO: check if the mention is valid

        //if we have a text before the mention, add the text before it to the messageParts
        if (mentionIndex > currentIndex) {
            messageParts.push(new MessagePart(MessagePartType.plain, message.substring(currentIndex, mentionIndex)));
        }

        //add the mention to the messageParts array
        const mentionNumber = possibleMention.substring(possibleMention.indexOf("#"));
        const mentionName = possibleMention.substring(1, possibleMention.indexOf("#"));
        const mentionAtsign = possibleMention.substring(0, 1);
        messageParts.push(new MessagePart(MessagePartType.mentionAtsign, mentionAtsign));
        messageParts.push(new MessagePart(MessagePartType.mentionName, mentionName));
        messageParts.push(new MessagePart(MessagePartType.mentionNumber, mentionNumber));



        //update the currentIndex
        currentIndex = endOfMentionIndex;
        //find the next @
        mentionIndex = message.indexOf("@", currentIndex);
    }
    //add the text after the last mention to the messageParts
    if (currentIndex < message.length) {
        messageParts.push(new MessagePart(MessagePartType.plain, message.substring(currentIndex)));
    }
    return messageParts;
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
