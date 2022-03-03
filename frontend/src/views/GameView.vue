<template>
  <div class="home">
    <LadderWindow />
    <ChatWindow />
    <InfoBox />
  </div>
</template>

<script>
// @ is an alias to /src
import LadderWindow from "@/components/LadderWindow";
import ChatWindow from "@/components/ChatWindow";
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
