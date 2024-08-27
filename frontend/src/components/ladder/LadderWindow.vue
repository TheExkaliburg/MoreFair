<template>
  <div
    class="flex flex-col justify-between items-center relative px-1.5 p-1 overflow-hidden text-text max-h-full"
  >
    <LadderWindowHeader class="h-8 w-full shrink-0" />
    <LadderWindowTable class="w-full flex-auto" />
    <div class="flex flex-col sm:flex-row-reverse justify-between w-full">
      <LadderWindowBody class="w-full flex-1" />
      <LadderWindowMenus class="w-full flex-1 md:mr-2 md:-ml-2" />
    </div>
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
  // border: 1px solid white;
}
</style>
