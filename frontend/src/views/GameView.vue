<template>
  <div class="game-view container">
    <div class="row py-0" style="height: 100%; max-height: 100%">
      <div class="col-7">
        <div class="container py-1 px-3">
          <LadderWindow />
          <InfoBox />
        </div>
      </div>
      <div class="col-5">
        <ChatWindow />
      </div>
    </div>
  </div>
</template>

<script setup>
// @ is an alias to /src
import LadderWindow from "@/ladder/components/LadderWindow";
import ChatWindow from "@/chat/components/ChatWindow";
import InfoBox from "@/ladder/components/InfoBox";
</script>

<style lang="scss" scoped>
@import "../styles/styles";

.game-view {
  max-width: 100vw;
  height: calc(100vh - 56px);

  div {
    max-height: 100%;

    &.container {
      height: 100%;
    }
  }
}

.navbar {
  height: 56px;
}
</style>
