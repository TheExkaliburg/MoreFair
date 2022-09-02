<template>
  <div>
    <CBrandedNavbarWithNarrowExpandableSidebar />
    <slot />
  </div>
</template>

<script setup lang="ts"></script>

<script>
import CBrandedNavbarWithNarrowExpandableSidebar from "../components/Navbar/CBrandedNavbarWithNarrowExpandableSidebar";
export default {
  components: { CBrandedNavbarWithNarrowExpandableSidebar },
};
</script>
<style scoped></style>
