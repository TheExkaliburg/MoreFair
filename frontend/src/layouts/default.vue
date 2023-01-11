<template>
  <div class="flex flex-col h-screen">
    <TheBrandedNavBarWithOffCanvas />
    <TheSidebar class="restOfVerticalScreen">
      <div class="restOfHorizontalScreen">
        <slot />
      </div>
    </TheSidebar>
  </div>
</template>

<script lang="ts" setup>
import TheSidebar from "~/components/navbar/TheSidebar.vue";
import TheBrandedNavBarWithOffCanvas from "~/components/navbar/TheBrandedNavBarWithOffCanvas.vue";
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
