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
import {
  Message,
  MessagePart,
  MessagePartType,
} from "~/store/entities/message";

const props = defineProps({
  message: {
    type: Message,
    required: true,
  },
});

const messageParts = computed<MessagePart[]>(() => {
  return props.message.getMessageParts();
});
</script>
