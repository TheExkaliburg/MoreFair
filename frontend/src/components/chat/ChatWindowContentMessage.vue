<template>
  <div class="flex flex-col w-full">
    <div class="flex flex-row text-sm justify-between pr-3">
      <span class="text-text-light truncate basis-5/8">
        <font-awesome-icon
          v-if="message.isMod"
          v-tippy="{ content: 'MOD', placement: 'right' }"
          class="text-text-mod"
          icon="fa-solid fa-shield-halved"
        />
        <span class="cursor-pointer" @click="mention">
          {{ message.username }}
          <sub class="text-text-dark"
            >&nbsp;#{{ message.accountId }}
          </sub></span
        >
      </span>
      <span class="text-text-light basis-1/8 flex-auto">
        <strong>{{ message.tag }}</strong
        ><sub
          v-if="optionsStore.state.general.showAssholePoints.value"
          class="text-text-dark"
          >{{ message.assholePoints }}</sub
        ></span
      >
      <span class="basis-1/4">
        {{ message.getTimestampString() }}
      </span>
      <span v-if="false" class="text-end flex-none">...</span>
    </div>
    <ChatWindowContentMessageBody :message="message" />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessageBody from "../../components/chat/ChatWindowContentMessageBody.vue";
import { Message } from "~/store/entities/message";
import { useOptionsStore } from "~/store/options";
import { useChatStore } from "~/store/chat";

const optionsStore = useOptionsStore();
const chatStore = useChatStore();

const props = defineProps({
  message: {
    type: Message,
    required: true,
  },
});

function mention() {
  const content = [
    {
      type: "userMention",
      attrs: {
        id: {
          username: props.message.username,
          accountId: props.message.accountId,
        },
        label: null,
      },
    },
    { type: "text", text: " " },
  ];
  const baseContentArray = chatStore.state.input.content;
  if (baseContentArray && baseContentArray.length === 1) {
    if (baseContentArray[0].content === undefined) {
      baseContentArray[0].content = content;
    } else {
      baseContentArray[0].content.push(...content);
    }
  }

  chatStore.state.input.content = baseContentArray;
}
</script>
