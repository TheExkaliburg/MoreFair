<template>
  <div class="chat-window container rounded py-1 px-3">
    <div class="chat-header row py-1">
      <!--div class="col chat-info">Chad #{{ chat.currentChatNumber }}</div-->
      <PaginationGroup
        :current="chat.currentChatNumber"
        :max="
          store.getters['options/getOptionValue']('enableUnrestrictedAccess') &&
          store.getters.isMod
            ? Math.max(settings.assholeLadder, user.highestCurrentLadder)
            : user.highestCurrentLadder
        "
        :onChange="changeChat"
      />
    </div>
    <div ref="chatContent" class="chat-content row py-0">
      <ChatMessage
        v-for="message in chat.messages"
        :key="message"
        :msg="message"
      />
    </div>
    <div class="chat-input row py-3">
      <ChatInput />
    </div>
    <div id="mentionDropdown" class="mentionDropdown"></div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, onUpdated, ref } from "vue";
import ChatMessage from "@/chat/components/ChatMessage";
import ChatInput from "@/chat/components/ChatInput";
import PaginationGroup from "@/components/PaginationGroup";

import { Sounds } from "@/modules/sounds";

Sounds.register(
  "mention",
  "https://assets.mixkit.co/sfx/download/mixkit-software-interface-start-2574.wav"
);

const store = useStore();
const stompClient = inject("$stompClient");

const chatContent = ref(null);

const chat = computed(() => store.state.chat.chat);
const user = computed(() => store.state.user);
const settings = computed(() => store.state.settings);

function changeChat(event) {
  const targetChat = event.target.dataset.number;
  if (targetChat !== chat.value.currentChatNumber) {
    stompClient.unsubscribe("/topic/chat/" + chat.value.currentChatNumber);
    stompClient.subscribe("/topic/chat/" + targetChat, (message) => {
      store.commit({ type: "chat/addMessage", message: message });
    });
    stompClient.send("/app/chat/init/" + targetChat);
  }
}

onUpdated(() => {
  chatContent.value.scrollTop = chatContent.value.scrollHeight;
});
</script>

<style lang="scss">
.mention {
  background-color: rgb(70, 70, 70);
  padding: 2px;
  border: 1px solid black;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}
</style>

<style lang="scss" scoped>
@import "../../styles/styles";
// .
.chat-window {
  height: 100%;
}

.mentionDropdown {
  display: block;
  position: absolute;
  background: var(--background-color);
  border: 1px solid var(--main-color);
  border-radius: 5px;
  z-index: 1000;
  padding: 5px;
  overflow-y: scroll;
}

.chat-content {
  overflow-y: auto;
  overflow-x: hidden;
  align-content: start;
  height: calc(100% - calc(70px + 60px));
}

.chat-header {
  max-height: 48px;
}
</style>
