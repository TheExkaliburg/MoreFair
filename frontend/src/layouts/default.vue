<template>
  <div>
    <CBrandedNavbarWithNarrowExpandableSidebar>
      <slot />
      Test123
    </CBrandedNavbarWithNarrowExpandableSidebar>
  </div>
</template>

<script setup lang="ts"></script>

<script>
import CBrandedNavbarWithNarrowExpandableSidebar from "../components/navbar/CBrandedNavbarWithNarrowExpandableSidebar";
export default {
  components: { CBrandedNavbarWithNarrowExpandableSidebar },
};
</script>
<style scoped></style>
