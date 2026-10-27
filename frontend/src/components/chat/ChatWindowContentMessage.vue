<template>
  <div ref="el" class="flex flex-col w-full">
    <div
      class="rounded-tl bg-gradient-to-br to-75% pl-px pt-px"
      :class="{
        'from-teal-600': message.chatType === ChatType.LADDER,
        'from-orange-700': message.chatType === ChatType.SYSTEM,
        'from-violet-600': message.chatType === ChatType.MOD,
      }"
    >
      <div class="rounded-tl bg-background pl-1 pt-0.5">
        <div class="flex flex-row text-sm justify-between pr-3">
          <span class="text-text-light truncate basis-[62.5%]">
            <span class="cursor-pointer" @click="mention">
              <span v-tippy="{ content: message.chatType }">{{
                message.getChatTypeIdentifier()
              }}</span
              >:
              <font-awesome-icon
                v-if="message.isMod"
                v-tippy="{ content: 'MOD', placement: 'right' }"
                class="text-text-mod"
                icon="fa-solid fa-shield-halved"
              />
              {{ message.username }}
              <sub class="text-text-dark"
                >&nbsp;#{{ message.accountId }}
              </sub></span
            >
          </span>
          <span class="text-text-light basis-[12.5%] flex-auto">
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
    </div>
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessageBody from "../../components/chat/ChatWindowContentMessageBody.vue";
import type { MessageView } from "~/store/entities/message";
import { useOptionsStore } from "~/store/options";
import { ChatType, useChatStore } from "~/store/chat";

const optionsStore = useOptionsStore();
const chatStore = useChatStore();

const props = defineProps<{ message: MessageView; index: number }>();

const el = ref<null | HTMLElement>(null);

const isLastMessage = computed<boolean>(() => {
  return props.index === chatStore.getters.allMessages.length - 1;
});

onMounted(() => {
  const parent = el.value?.parentElement;
  let wasScrolledDown = true;
  if (parent) {
    wasScrolledDown =
      parent.scrollTop + parent.clientHeight >= parent.scrollHeight - 300;
  }

  if (isLastMessage.value && wasScrolledDown) {
    requestAnimationFrame(() => {
      el.value?.parentElement?.scrollTo({ top: el.value.offsetTop });
    });
  }
});

function mention() {
  const content = [
    {
      type: "userMention",
      attrs: {
        id: {
          displayName: props.message.username,
          accountId: props.message.accountId,
        },
        label: null,
      },
    },
    { type: "text", text: " " },
  ];
  const baseContentArray = chatStore.state.input.content;
  const paragraph = baseContentArray?.[0];
  if (baseContentArray?.length === 1 && paragraph) {
    if (paragraph.content === undefined) {
      paragraph.content = content;
    } else {
      paragraph.content.push(...content);
    }
  }

  chatStore.state.input.content = baseContentArray;
}
</script>
