<template>
  <div>
    <template v-for="part in messageParts" :key="part">
      <span v-if="part.is(MessagePartType.plain)">{{ part.text }}</span>
      <span
        v-else-if="part.is(MessagePartType.mentionUser)"
        class="text-text-light"
        >@{{ part.text }}
      </span>
      <sub
        v-else-if="part.is(MessagePartType.mentionUserId)"
        class="text-text-dark"
        >#{{ part.text }}
      </sub>
      <span v-else-if="part.is(MessagePartType.mentionGroup)"
        ><span class="text-text-dark">$</span>{{ part.text
        }}<span class="text-text-dark">$</span>
      </span>
      <span v-else class="text-red-500">
        {{ part.text }}
      </span>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
});

enum MessagePartType {
  plain,
  mentionUser,
  mentionUserId,
  mentionGroup,
}

class MessagePart {
  public readonly type: MessagePartType;
  public readonly text: string;

  constructor(type, text) {
    this.type = type;
    this.text = text;
  }

  is(type) {
    if (this.type === type) return true;
  }
}

const messageParts = computed<MessagePart[]>(() => {
  const message = props.message.message;
  const metadata = props.message.metadata;
  const result: MessagePart[] = [];

  if (!metadata) {
    return [new MessagePart(MessagePartType.plain, message)];
  }

  const mentions = metadata.filter((m) => "u" in m && "id" in m && "i" in m);
  const groupMentions = metadata.filter((m) => "g" in m && "i" in m);
  const combinedMentions = [...mentions, ...groupMentions].sort(
    (a, b) => a.i - b.i
  );

  let lastIndex = 0;
  combinedMentions.forEach((m) => {
    const isGroup = Boolean(m.g);
    const index = m.i;
    if (isGroup) {
      let name = m.g;
      name = name.trim();
      if (message.slice(index, index + 3) !== "{$}") return;

      result.push(
        new MessagePart(MessagePartType.plain, message.slice(lastIndex, index))
      );
      result.push(new MessagePart(MessagePartType.mentionGroup, name));
      lastIndex = index + 3;
    } else {
      const name = m.u.trim();
      const id = m.id;
      if (message.slice(index, index + 3) !== "{@}") return;

      result.push(
        new MessagePart(MessagePartType.plain, message.slice(lastIndex, index))
      );
      result.push(new MessagePart(MessagePartType.mentionUser, name));
      result.push(new MessagePart(MessagePartType.mentionUserId, id));
      lastIndex = index + 3;
    }
  });

  result.push(new MessagePart(MessagePartType.plain, message.slice(lastIndex)));
  return result;
});
</script>
