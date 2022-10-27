<template>
  <div
    class="bg-background flex flex-col justify-center items-center relative p-1"
  >
    <ChatWindowHeader class="h-8 w-full shrink-0"></ChatWindowHeader>
    <ChatWindowContent
      class="flex-auto w-full items-center"
    ></ChatWindowContent>
    <ChatWindowInput class="h-12 lg:h-16 w-full shrink-0" />
  </div>
</template>

<script lang="ts" setup></script>

<style lang="scss" scoped>
div {
  border: 0px solid black;
}
</style>
