<template>
  <div
    class="flex flex-col justify-center items-center relative px-1.5 p-1 overflow-hidden"
  >
    <ChatWindowHeader
      class="absolute -inset-x-1 top-1 w-full shrink-0 h-8 lg:static"
    />
    <ChatWindowContent class="h-full w-full flex-auto items-center" />
    <ChatWindowInput class="h-12 lg:h-13 w-full shrink-0 text-md" />
  </div>
</template>

<script lang="ts" setup></script>
