<template>
  <div ref="el" class="flex flex-col w-full">
    <div class="flex flex-row text-sm justify-between pr-3">
      <span class="text-text-light truncate basis-5/8">
        <span>
          L{{ message.chatNumber }}:
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
      <span class="text-end flex-none">...</span>
    </div>
    <ChatWindowContentMessageBody :message="message" />
  </div>
</template>

<script lang="ts" setup>
import ChatWindowContentMessageBody from "../../components/chat/ChatWindowContentMessageBody.vue";
import { useOptionsStore } from "~/store/options";
import { ChatLogMessage, useModerationStore } from "~/store/moderation";

const optionsStore = useOptionsStore();
const moderationStore = useModerationStore();

const props = defineProps<{
  message: ChatLogMessage;
  index: number;
}>();

const el = ref<null | HTMLElement>(null);

const isLastMessage = computed<boolean>(() => {
  return props.index === moderationStore.state.chatLog.length - 1;
});

onMounted(() => {
  if (isLastMessage.value) {
    el.value?.scrollIntoView();
  }
});
</script>

<style scoped></style>
