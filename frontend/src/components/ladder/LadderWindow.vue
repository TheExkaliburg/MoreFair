<template>
  <div
    class="flex flex-col justify-between items-center relative px-1.5 p-1 overflow-hidden text-text max-h-full"
  >
    <LadderWindowHeader class="h-8 w-full shrink-0" />
    <LadderWindowTable class="w-full flex-auto" />
    <LadderWindowBody class="w-full shrink-0" />
    <LadderWindowMenus class="w-full shrink-0" />
  </div>
</template>

<script lang="ts" setup>
import LadderWindowHeader from "./LadderWindowHeader.vue";
import LadderWindowTable from "./LadderWindowTable.vue";
import LadderWindowBody from "./LadderWindowBody.vue";
import LadderWindowMenus from "./LadderWindowMenus.vue";
</script>

<style lang="scss" scoped>
div {
  //border: 1px solid white;
}
</style>
