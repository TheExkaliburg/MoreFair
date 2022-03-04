<template>
  <div class="chat-window container rounded px-3 py-1">
    <div class="chat-header row py-0">
      <div class="col chat-info">Chad #5</div>
      <div class="col chat-pagination">
        <div class="btn-group dropdown">
          <button class="btn btn-outline-primary shadow-none">&laquo;</button>
          <button class="btn btn-outline-primary shadow-none">4</button>
          <button class="btn btn-outline-primary shadow-none active">5</button>
          <button class="btn btn-outline-primary shadow-none">6</button>
          <button class="btn btn-outline-primary shadow-none">&raquo;</button>
          <button
            class="btn btn-outline-primary shadow-none dropdown-toggle"
            data-bs-toggle="dropdown"
          ></button>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="#">1</a></li>
            <li><a class="dropdown-item" href="#">2</a></li>
            <li><a class="dropdown-item" href="#">3</a></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="chat-content row py-0">
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
      <ChatMessage />
    </div>
    <div class="chat-input row py-3">
      <div class="input-group">
        <input
          class="form-control shadow-none"
          placeholder="Chad is listening..."
        />
        <button class="btn btn-outline-primary shadow-none">Send</button>
      </div>
    </div>
  </div>
</template>

<script>
import ChatMessage from "@/chat/components/ChatMessages";

export default {
  name: "ChatWindow",
  components: { ChatMessage },
};
</script>

<style lang="scss">
@import "../../styles/styles";
// .
.chat-window {
  height: 100%;
}

.chat-pagination {
  text-align: end;
  padding-right: 0px;
}

.chat-content {
  overflow: auto;
  height: 90%;
  max-height: 90%;
}
</style>
