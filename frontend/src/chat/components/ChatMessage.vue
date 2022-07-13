<template>
  <div class="container px-3 py-1 message">
    <div class="row py-0 message-header">
      <div class="col-4 message-username">
        <span
          class="message-user-name"
          style="cursor: pointer"
          @click="mentionUser"
        >
          {{ msg.username }}
        </span>
        <sub style="cursor: pointer" @click="mentionUser"
          >&nbsp;#{{ msg.accountId }}</sub
        >
      </div>
      <div class="col-4 message-status">
        <strong>{{ msg.tag }}</strong
        ><sub>{{ msg.tag === "" ? "" : msg.ahPoints }}</sub>
      </div>
      <div class="col-3 message-date">{{ msg.timeCreated }}</div>
      <div
        v-if="
          store.getters['options/getOptionValue']('enableChatModFeatures') &&
          store.getters.isMod
        "
        class="col-1 message-options"
      >
        <a data-bs-toggle="dropdown">...</a>
        <ul class="dropdown-menu">
          <span
            style="
              color: var(--text-color);
              padding: 5px;
              margin: 0px;
              width: 100%;
              display: inline-block;
            "
            >{{ msg.username
            }}<sub style="color: var(--text-dark-highlight-color)"
              >#{{ msg.accountId }}</sub
            ></span
          >
          <li>
            <a class="dropdown-item" href="#" @click="ban">Ban</a>
            <a class="dropdown-item" href="#" @click="mute">Mute</a>
            <a class="dropdown-item" href="#" @click="rename">Rename</a>
            <a class="dropdown-item" href="#" @click="free">Free</a>
          </li>
        </ul>
      </div>
    </div>
    <div class="row py-0 message-body">
      <ChatMessageBody :msg="msg" />
    </div>
  </div>
</template>

<script setup>
import { defineProps, inject } from "vue";
import { useStore } from "vuex";
import ChatMessageBody from "@/chat/components/ChatMessageBody";
import API from "@/websocket/wsApi";
import { getMentionElement, insertSpecialChatElement } from "./ChatInput.vue";

const store = useStore();
const stompClient = inject("$stompClient");

const props = defineProps({
  msg: Object,
});

function mentionUser() {
  let mention = getMentionElement({
    name: props.msg.username,
    id: props.msg.accountId,
  });
  insertSpecialChatElement(mention, "END", "END");
}

function ban() {
  if (
    confirm(
      `Are you sure you want to ban "${props.msg.username}" (#${props.msg.accountId})`
    )
  ) {
    stompClient.send(API.MODERATION.APP_BAN_DESTINATION(props.msg.accountId));
  }
}

function mute() {
  if (
    confirm(
      `Are you sure you want to mute "${props.msg.username}" (#${props.msg.accountId})`
    )
  ) {
    stompClient.send(API.MODERATION.APP_MUTE_DESTINATION(props.msg.accountId));
  }
}

function rename() {
  const newName = prompt(
    `What would you like to name "${props.msg.username}" (#${props.msg.accountId})`
  );
  if (newName) {
    stompClient.send(
      API.MODERATION.APP_RENAME_DESTINATION(props.msg.accountId),
      {
        content: newName,
      }
    );
  }
}

function free() {
  if (
    confirm(
      `Are you sure you want to free "${props.msg.username}" (#${props.msg.accountId})`
    )
  ) {
    stompClient.send("/app/mod/free/" + props.msg.accountId);
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/styles";

.message {
  // border: white solid 0.5px;
}

sub {
  color: var(--text-dark-highlight-color);
}

.message-header {
  font-size: var(--header-font-size);
  color: var(--main-color);
  white-space: nowrap;
}

.message-username {
  text-align: start;
  overflow: hidden;
}

.dropdown-menu {
  border: 1px solid var(--secondary-color);
  border-radius: 0.25rem;
  padding: 0px;
  margin: 0px;
}

.message-options {
  text-align: end;

  a {
    text-decoration: none !important;
    color: var(--main-color) !important;
  }

  a:visited {
    color: var(--main-color) !important;
  }

  a:hover {
    cursor: pointer;
  }
}

.message-body {
  font-size: var(--text-font-size);
  text-align: start;
  color: var(--text-color);
}
</style>
