<template>
  <div>
    <div>{{ titleText }}</div>
    <NuxtWelcome />
  </div>
</template>

<script setup lang="ts">
const lang = useLang();

const titleText = computed<string>(() => {
  return lang("pages.index.title");
});
</script>

<style scoped></style>
