<template>
  <div class="container px-3 py-1 message">
    <div class="row py-0 message-header">
      <div class="col-4 message-username">
        <span class="message-user-name">Kali </span>
        <span class="message-user-id">#11235</span>
      </div>
      <div class="col-3 message-date">Tue, 19:53</div>
      <div class="col-4 message-status">[4][1][2][3]</div>
      <div class="col-1 message-options">...</div>
    </div>
    <div class="row py-0 message-body">
      <span
        >Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam
        nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,
        sed diam voluptua. At vero eos et accusam et justo duo dolores et ea
        rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem
        ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing
        elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna
        aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo
        dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus
        est Lorem ipsum dolor sit amet.
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: "ChatMessages",
};
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
  font-size: 11.4px;
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
  font-size: 12.5px;
  text-align: start;
  color: #e7d5b3;
}
</style>
