<template>
  <div class="flex flex-row justify-center text-text h-full w-full">
    <TheVinegarThrowTable :rows="50" class="w-reader overflow-y-scroll" />
  </div>
</template>

<script setup lang="ts">
import TheVinegarThrowTable from "~/components/grapes/TheVinegarThrowTable.vue";

useSeoMeta({
  title: "FairGame - VinegarThrowLog",
  description: "The Log for Vinegar Throws",
});
</script>
