<template>
  <div class="container px-3 py-1 message">
    <span v-for="part in messageParts" :key="part">
      <!-- Plain Message -->
      <span v-if="part.is(MessagePartType.plain)">
        {{ part.text }}
      </span>
      <!-- Mentions -->
      <span
        v-else-if="part.is(MessagePartType.mentionAtsign)"
        class="chat-mention-at"
      >
        {{ part.text }}
      </span>
      <span
        v-else-if="part.is(MessagePartType.mentionName)"
        class="chat-mention"
      >
        {{ part.text }}
      </span>
      <span
        v-else-if="part.is(MessagePartType.mentionGroupBoundary)"
        class="chat-mention-group-boundary"
      >
        {{ part.text }}
      </span>
      <sub
        v-else-if="part.is(MessagePartType.mentionNumber)"
        class="chat-mention-user-id"
      >
        {{ part.text }}
      </sub>
      <!-- Unimplemented -->
      <span v-else class="chat-unknown-part-type">
        {{ part.text }}
      </span>
    </span>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, defineProps } from "vue";
import { Sounds } from "@/modules/sounds";

const props = defineProps({
  msg: Object,
});

const store = useStore();

const rankers = computed(() => store.getters["ladder/allRankers"]);

//const numberFormatter = computed(() => store.state.numberFormatter);
//const ladder = computed(() => store.state.ladder);
/*const highlightMentions = computed(() =>
  store.getters["options/getOptionValue"]("highlightMentions")
);*/

const mentionSound = computed(() =>
  store.getters["options/getOptionValue"]("mentionSound")
);

const mentionSoundVolume = computed(() =>
  store.getters["options/getOptionValue"]("notificationVolume")
);

//Basically an enum
const MessagePartType = {
  plain: Symbol("plain"),
  mentionName: Symbol("mentionName"),
  mentionGroupBoundary: Symbol("mentionGroupBoundary"),
  mentionNumber: Symbol("mentionNumber"),
  mentionAtsign: Symbol("mentionAtsign"),
};

class MessagePart {
  constructor(type, text) {
    this.type = type;
    this.text = text;
  }

  is(type) {
    if (this.type === type) return true;
    if (type === MessagePartType.plain) {
      /*Here we could disable the different types of highlighting
      if (!highlightMentions.value) {
        return (
          this.type === MessagePartType.mentionName ||
          this.type === MessagePartType.mentionNumber ||
          this.type === MessagePartType.mentionAtsign
        );
      }*/
    }
  }
}

const messageParts = [
  new MessagePart(MessagePartType.plain, props.msg.message),
];

function spliceNewMessagePartsIntoArray(oldPart, newParts) {
  messageParts.splice(messageParts.indexOf(oldPart), 1, ...newParts);
}

function findMentions() {
  const msg = props.msg;
  let meta = msg.metadata;
  if (!meta) {
    return;
  }
  //const message = msg.message;
  const mentions = meta.filter((m) => {
    let isOk = false;
    try {
      isOk = "u" in m && "id" in m && "i" in m;
    } catch (e) {
      //This is a check for the case that the m is not an object
      //We get really weird errors when we dont catch this but we dont care about the error here.
    }
    return isOk;
  });
  const groupMentions = meta.filter((m) => {
    let isOk = false;
    try {
      isOk = "g" in m && "i" in m;
      isOk &= m.g.length <= 20; //We dont want to parse too long groups...
    } catch (e) {
      //This is a check for the case that the m is not an object
      //We get really weird errors when we dont catch this but we dont care about the error here.
    }
    return isOk;
  });
  mentions.sort((a, b) => a.i - b.i);
  groupMentions.sort((a, b) => a.i - b.i);
  const combinedMentions = [...mentions, ...groupMentions];
  combinedMentions.sort((a, b) => a.i - b.i);
  let offset = 0;
  let currentPlainText = messageParts[0];
  combinedMentions.forEach((m) => {
    const group = m.g ? true : false;
    let newParts = [];
    let index = m.i - offset;
    let id = -1;
    if (group) {
      let name = m.g;
      name = name.trim();

      if (currentPlainText.text.slice(index, index + 3) !== "{$}") {
        return;
      }

      newParts = [
        new MessagePart(
          MessagePartType.plain,
          currentPlainText.text.slice(0, index)
        ),
        new MessagePart(MessagePartType.mentionGroupBoundary, "$"),
        new MessagePart(MessagePartType.mentionName, name),
        new MessagePart(MessagePartType.mentionGroupBoundary, "$"),
        new MessagePart(
          MessagePartType.plain,
          currentPlainText.text.slice(index + 3)
        ),
      ];
    } else {
      id = parseInt(m.id);
      let name = m.u;
      name = name.trim();

      if (currentPlainText.text.slice(index, index + 3) !== "{@}") {
        return;
      }

      newParts = [
        new MessagePart(
          MessagePartType.plain,
          currentPlainText.text.slice(0, index)
        ),
        new MessagePart(MessagePartType.mentionAtsign, "@"),
        new MessagePart(MessagePartType.mentionName, name),
        new MessagePart(MessagePartType.mentionNumber, "#" + id),
        new MessagePart(
          MessagePartType.plain,
          currentPlainText.text.slice(index + 3)
        ),
      ];
    }

    spliceNewMessagePartsIntoArray(currentPlainText, newParts);
    offset += index + 3;
    currentPlainText = newParts[4];
    if (
      mentionSound.value &&
      !msg.hasFlag("mentionSoundPlayed") &&
      !msg.hasFlag("old")
    ) {
      //Mark the message as having played the sound even if it does not mention us.
      //This is to prevent it from playing when we dont expect it to.
      store.commit("chat/msgFlag", {
        message: msg,
        flag: "mentionSoundPlayed",
        type: "set",
      });
      /**@type Array<String> */
      let subMentions =
        store.getters["options/getOptionValue"]("subscribedMentions");

      if (
        group &&
        subMentions.map((m) => m.toLowerCase()).includes(m.g.toLowerCase())
      ) {
        Sounds.play("mention", mentionSoundVolume.value);
      }
      for (let i = 0; i < rankers.value.length; i++) {
        if (rankers.value[i].you && rankers.value[i].accountId === id) {
          Sounds.play("mention", mentionSoundVolume.value);
        }
      }
    }
  });
}

findMentions();
</script>

<style lang="scss" scoped>
@import "../../styles/styles";

.chat-mention-at,
.chat-mention {
  color: var(--main-color);
}

//This hurts my eyes and exactly that is what I want.
//Because this is shown when we forget to implement a part type
.chat-unknown-part-type {
  font-weight: bold;
  color: white;
  background-color: red;
}

.chat-mention-group-boundary {
  color: var(--text-dark-highlight-color);
  opacity: 0.5;
}

.chat-mention-user-id {
  color: var(--text-dark-highlight-color);
}
</style>
