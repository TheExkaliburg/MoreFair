<template>
  <div>
    <TheBrandedNavBarWithOffCanvas class="xl:hidden" />
    <TheBrandedExpandableSidebar><slot /></TheBrandedExpandableSidebar>
  </div>
</template>

<script setup lang="ts">
import TheBrandedExpandableSidebar from "~/components/navbar/TheBrandedExpandableSidebar.vue";
import TheBrandedNavBarWithOffCanvas from "~/components/navbar/TheBrandedNavBarWithOffCanvas.vue";
</script>
