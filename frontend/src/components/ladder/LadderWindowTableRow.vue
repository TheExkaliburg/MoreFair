<template>
  <div
    v-if="active"
    :class="{ 'text-text-light': !ranker.growing }"
    class="flex flex-row flex-nowrap justify-between items-center"
  >
    <div class="w-full">
      {{ ranker.rank }} {{ ranker.assholeTag }}
      <sub class="text-text-dark">{{ ranker.assholePoints }}</sub>
    </div>
    <div class="w-full">
      {{ ranker.username }}<sub class="text-text-dark">#{{ ranker.id }}</sub>
    </div>
    <div class="w-full text-right">{{ formattedPower }}</div>
    <div class="w-full text-right">{{ formattedPoints }}</div>
  </div>
</template>

<script lang="ts" setup>
import { Ranker } from "~/store/entities/ranker";
import { useFormatter } from "~/composables/useFormatter";

const props = defineProps({
  ranker: { type: Ranker, required: true },
  active: { type: Boolean, required: false, default: true },
  index: { type: Number, required: false, default: -1 },
});

const formattedPower = computed(() => {
  return useFormatter(props.ranker.power);
});
const formattedPoints = computed(() => {
  return useFormatter(props.ranker.points);
});
</script>

<style scoped></style>
