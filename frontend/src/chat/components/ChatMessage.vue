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
import { useStore } from "vuex";
import { defineProps } from "vue";

const props = defineProps({
  msg: Object,
});

const store = useStore();

//const numberFormatter = computed(() => store.state.numberFormatter);
//const ladder = computed(() => store.state.ladder);
const rankers = store.getters["ladder/shownRankers"]

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

    const sortedUsers = rankers.sort((a, b) => {
        const nameCompare = a.username.localeCompare(b.username);
        if (nameCompare !== 0) {
            return nameCompare;
        }
        return b.accountId - a.accountId;
    });

    sortedUsers.forEach(user => {
        findMention(user);
    });
}

function findMention(user) {
    for(let messagePart of messageParts) {
        if (messagePart.is(MessagePartType.plain)) {
            const mention = messagePart.text.indexOf(`@${user.username}#${user.accountId}`);
            if (mention !== -1) {
                const preMessagePart = new MessagePart(MessagePartType.plain, messagePart.text.substring(0, mention));
                const mentionAtsign = new MessagePart(MessagePartType.mentionAtsign, "@");
                const mentionName = new MessagePart(MessagePartType.mentionName, user.username);
                const mentionNumber = new MessagePart(MessagePartType.mentionNumber, '#'+user.accountId);
                const postMessagePart = new MessagePart(MessagePartType.plain, messagePart.text.substring(mention+`@${user.username}#${user.accountId}`.length));
                spliceNewMessagePartsIntoArray(messagePart, [preMessagePart, mentionAtsign, mentionName, mentionNumber, postMessagePart]);
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
