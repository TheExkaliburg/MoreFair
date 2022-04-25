<template>
  <div class="container px-3 py-1 message">
    <div class="row py-0 message-header">
      <div class="col-4 message-username">
        <span class="message-user-name">
          <span v-html="msg.username" />
        </span>
        <span class="message-user-id">&nbsp;#{{ msg.accountId }}</span>
      </div>
      <div class="col-4 message-status">
        <strong>{{
          msg.timesAsshole > 0
            ? "[" + settings.assholeTags[msg.timesAsshole + 1] + "]"
            : ""
        }}</strong>
      </div>
      <div class="col-3 message-date">{{ msg.timeCreated }}</div>
      <div
        v-if="accessRole === 'OWNER' || accessRole === 'MODERATOR'"
        class="col-1 message-options"
      >
        <a data-bs-toggle="dropdown">...</a>
        <ul class="dropdown-menu">
          <li>
            <a class="dropdown-item" href="#" @click="onChange">test</a>
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
import { computed, defineProps } from "vue";
import { useStore } from "vuex";
import ChatMessageBody from "@/chat/components/ChatMessageBody";

const store = useStore();
const settings = computed(() => store.state.settings);
const accessRole = computed(() => store.state.user.accessRole);

defineProps({
  msg: Object,
});
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
}

.message-body {
  font-size: $text-font-size;
  text-align: start;
  color: $text-color;
}
</style>
