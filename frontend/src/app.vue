<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script lang="ts" setup>
import Fair from "~/utils/hooks";

useAPI();

window.Fair = Fair;
</script>
