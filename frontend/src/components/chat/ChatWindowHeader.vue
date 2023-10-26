<template>
  <div class="flex items-center justify-end text-button-text">
    <!--PaginationButtonGroup
      :current="chatStore.state.number"
      :max="maxChat"
      :prefix="'Chad'"
      class="h-8 w-42 self-end bg-background z-2"
      @change="(number) => chatStore.actions.changeChat(number)"
    /-->
    <!--ChatWindowHeaderSelector class="z-20 h-8 w-32 self-end bg-background" /-->
  </div>
</template>

<script lang="ts" setup>
// const chatStore = useChatStore();
// const accountStore = useAccountStore();
// const roundStore = useRoundStore();
/*
const maxChat = computed<number>(() =>
  accountStore.getters.isMod
    ? roundStore.state.assholeLadder + 1
    : accountStore.state.highestCurrentLadder
); */
</script>
