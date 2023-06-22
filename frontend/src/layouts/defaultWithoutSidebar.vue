<template>
  <div class="flex flex-col h-screen">
    <TheBrandedNavBarWithOffCanvas />
    <div class="restOfVerticalScreen w-screen">
      <slot />
    </div>
  </div>
</template>

<script lang="ts" setup>
import TheBrandedNavBarWithOffCanvas from "../components/navbar/TheBrandedNavBarWithOffCanvas.vue";
</script>

<style lang="scss" scoped>
.restOfVerticalScreen {
  height: calc(100vh - 2.5rem);
}

.restOfHorizontalScreen {
  @media (min-width: 640px) {
    width: calc(100vw - 2.5rem);
  }
}
</style>
