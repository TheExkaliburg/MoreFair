<template>
  <div class="flex flex-row flex-nowrap justify-between items-center">
    <div class="w-full">{{ ranker.rank }}</div>
    <div class="w-full">{{ ranker.username }}</div>
    <div class="w-full text-right">{{ formattedPower }}</div>
    <div class="w-full text-right">{{ formattedPoints }}</div>
  </div>
</template>

<script lang="ts" setup>
import { Ranker } from "~/store/entities/ranker";
import { useFormatter } from "~/composables/useFormatter";

const props = defineProps({
  ranker: { type: Ranker, required: true },
});

const formattedPower = computed(() => {
  return useFormatter(props.ranker.power);
});
const formattedPoints = computed(() => {
  return useFormatter(props.ranker.points);
});
</script>

<style scoped></style>
