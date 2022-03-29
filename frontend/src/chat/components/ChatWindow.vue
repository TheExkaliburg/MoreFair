<template>
  <div class="chat-window container rounded py-1 px-3">
    <div class="chat-header row py-1">
      <!--div class="col chat-info">Chad #{{ chat.currentChatNumber }}</div-->
      <div class="col chat-pagination">
        <div class="btn-group dropdown">
          <button
            :class="user.highestCurrentLadder === 1 ? 'disabled' : ''"
            class="btn btn-outline-primary shadow-none"
            data-chat="1"
            @click="changeChat"
          >
            &laquo;
          </button>
          <button
            :class="chat.currentChatNumber === 1 ? 'active' : ''"
            :data-chat="
              chat.currentChatNumber === 1
                ? chat.currentChatNumber
                : chat.currentChatNumber - 1
            "
            class="btn btn-outline-primary shadow-none"
            @click="changeChat"
          >
            {{
              chat.currentChatNumber === 1
                ? chat.currentChatNumber
                : chat.currentChatNumber - 1
            }}
          </button>
          <button
            :class="[
              user.highestCurrentLadder === 1 ? 'disabled' : '',
              chat.currentChatNumber === 1 ? '' : 'active',
            ]"
            :data-chat="
              chat.currentChatNumber === 1
                ? chat.currentChatNumber + 1
                : chat.currentChatNumber
            "
            class="btn btn-outline-primary shadow-none"
            @click="changeChat"
          >
            {{
              chat.currentChatNumber === 1
                ? chat.currentChatNumber + 1
                : chat.currentChatNumber
            }}
          </button>
          <button
            :class="
              user.highestCurrentLadder >=
              Math.max(chat.currentChatNumber, 2) + 1
                ? ''
                : 'disabled'
            "
            :data-chat="
              chat.currentChatNumber === 1
                ? chat.currentChatNumber + 2
                : chat.currentChatNumber + 1
            "
            class="btn btn-outline-primary shadow-none"
            @click="changeChat"
          >
            {{
              chat.currentChatNumber === 1
                ? chat.currentChatNumber + 2
                : chat.currentChatNumber + 1
            }}
          </button>
          <button
            :class="user.highestCurrentLadder === 1 ? 'disabled' : ''"
            :data-chat="user.highestCurrentLadder"
            class="btn btn-outline-primary shadow-none"
            @click="changeChat"
          >
            &raquo;
          </button>
          <button
            class="btn btn-outline-primary shadow-none dropdown-toggle"
            data-bs-toggle="dropdown"
          ></button>
          <ul class="dropdown-menu">
            <li v-for="i in user.highestCurrentLadder" :key="i">
              <a
                :data-chat="i"
                class="dropdown-item"
                href="#"
                @click="changeChat"
                >{{ i }}</a
              >
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div ref="chatContent" class="chat-content row py-0">
      <ChatMessage
        v-for="message in chat.messages"
        :key="message"
        :msg="message"
      />
    </div>
    <div class="chat-input row py-3">
      <div class="input-group" v-on:keydown.enter="sendMessage">
        <input
          id="chatInput"
          v-model="message"
          class="form-control shadow-none"
          placeholder="Chad is listening..."
          type="text"
        />
        <button
          class="btn btn-outline-primary shadow-none"
          @click="sendMessage"
        >
          Send
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, onUpdated, ref } from "vue";
import ChatMessage from "@/chat/components/ChatMessage";

const store = useStore();
const stompClient = inject("$stompClient");

const message = ref("");
const chatContent = ref(null);

const chat = computed(() => store.state.chat.chat);
const user = computed(() => store.state.user);

function sendMessage() {
  stompClient.send("/app/chat/post/" + chat.value.currentChatNumber, {
    content: message.value,
  });
  message.value = "";
}

function changeChat(event) {
  const targetChat = event.target.dataset.chat;
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

<style lang="scss" scoped>
@import "../../styles/styles";
// .
.chat-window {
  height: 100%;
}

.chat-pagination {
  white-space: nowrap;
  text-align: end;
  padding-right: 0px;
}

.chat-content {
  overflow-y: auto;
  overflow-x: hidden;
  align-content: start;
  height: calc(100% - calc(70px + 40px));
}

.chat-info {
  white-space: nowrap;
  overflow-x: hidden;
}

.chat-header {
  max-height: 48px;
}
</style>
