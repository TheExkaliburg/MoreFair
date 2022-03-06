<template>
  <div class="container px-3 py-1 message">
    <div class="row py-0 message-header">
      <div class="col-4 message-username">
        <span class="message-user-name">{{ msg.username }} </span>
        <span class="message-user-id">#{{ msg.accountId }}</span>
      </div>
      <div class="col-3 message-date">{{ msg.timeCreated }}</div>
      <div class="col-4 message-status">♠ - [🟢][⚙️][3]</div>
      <div class="col-1 message-options">...</div>
    </div>
    <div class="row py-0 message-body">
      <span> {{ msg.message }}</span>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from "vue";

defineProps({
  msg: Object,
});
</script>

<style lang="scss">
@import "../../styles/styles";

.message {
  //border: white solid 0.5px;
}

.message-user-id {
  color: #cf573c;
}

.message-header {
  font-size: 12px;
  color: $main-color;
}

.message-username {
  text-align: start;
}

.message-options {
  text-align: end;
}

.message-body {
  //ddd
  font-size: 14px;
  text-align: start;
  color: #e7d5b3;
}
</style>
