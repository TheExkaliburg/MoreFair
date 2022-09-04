<template>
  <div>
    <MobileBrandedNavBar class="xl:hidden" />
    <BrandedNarrowExpandableSidebar><slot /></BrandedNarrowExpandableSidebar>
  </div>
</template>

<script setup lang="ts">
import MobileBrandedNavBar from "~/components/navbar/BrandedNavBar.vue";
import BrandedNarrowExpandableSidebar from "~/components/navbar/BrandedNarrowExpandableSidebar.vue";
</script>

<style scoped lang="scss"></style>
