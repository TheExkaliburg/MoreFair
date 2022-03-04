<template>
  <div class="game-view container">
    <div class="row" style="height: 100%; max-height: 100%">
      <div class="col-7">
        <LadderWindow />
        <InfoBox />
      </div>
      <div class="col-5">
        <ChatWindow />
      </div>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import LadderWindow from "@/components/LadderWindow";
import ChatWindow from "@/chat/components/ChatWindow";
import InfoBox from "@/components/InfoBox";

export default {
  name: "HomeView",
  components: {
    LadderWindow,
    ChatWindow,
    InfoBox,
  },
};
</script>

<style lang="scss">
@import "../styles/styles";

.game-view {
  max-width: 100vw;
  height: calc(100vh - 56px);

  div {
    max-height: 100%;
  }
}

.navbar {
  height: 56px;
}
</style>
