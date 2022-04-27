<template>
  <div class="container px-3 py-1 message">
    <div class="row py-0 message-header">
      <div class="col-4 message-username">
        <span class="message-user-name">
          <span>{{ msg.username }}</span>
        </span>
        <span class="message-user-id">&nbsp;#{{ msg.accountId }}</span>
      </div>
      <div class="col-4 message-status">
        <strong>{{
          msg.timesAsshole > 0
            ? "[" + settings.assholeTags[msg.timesAsshole] + "]"
            : ""
        }}</strong>
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
import { computed, defineProps, inject } from "vue";
import { useStore } from "vuex";
import ChatMessageBody from "@/chat/components/ChatMessageBody";

const store = useStore();
const stompClient = inject("$stompClient");
const settings = computed(() => store.state.settings);

const props = defineProps({
  msg: Object,
});

function ban() {
  if (
    confirm(
      `Are you sure you want to ban "${props.msg.username}" (#${props.msg.accountId})`
    )
  ) {
    stompClient.send("/app/mod/ban/" + props.msg.accountId);
  }
}

function mute() {
  if (
    confirm(
      `Are you sure you want to mute "${props.msg.username}" (#${props.msg.accountId})`
    )
  ) {
    stompClient.send("/app/mod/mute/" + props.msg.accountId);
  }
}

function rename() {
  const newName = prompt(
    `What would you like to name "${props.msg.username}" (#${props.msg.accountId})`
  );
  if (newName) {
    stompClient.send("/app/mod/name/" + props.msg.accountId, {
      content: newName,
    });
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

.message-user-id {
  color: #cf573c;
}

.message-header {
  font-size: $header-font-size;
  color: $main-color;
  white-space: nowrap;
}

.message-username {
  text-align: start;
  overflow-x: hidden;
}

.message-options {
  text-align: end;

  a {
    text-decoration: none !important;
    color: $main-color !important;
  }

  a:visited {
    color: $main-color !important;
  }

  a:hover {
    cursor: pointer;
  }
}

.message-body {
  font-size: $text-font-size;
  text-align: start;
  color: $text-color;
}
</style>
