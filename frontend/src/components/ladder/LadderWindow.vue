<template>
  <div
    class="flex flex-col justify-center items-center relative p-1 overflow-hidden text-text"
  >
    <LadderWindowHeader class="h-8 w-full" />
    <LadderWindowTable class="h-full w-full" />
    <LadderWindowBody class="h-1/4 w-full" />
  </div>
</template>

<script lang="ts" setup>
import LadderWindowHeader from "./LadderWindowHeader.vue";
import LadderWindowTable from "./LadderWindowTable.vue";
import LadderWindowBody from "./LadderWindowBody.vue";
</script>

<style lang="scss" scoped>
div {
  border: 1px solid white;
}
</style>
