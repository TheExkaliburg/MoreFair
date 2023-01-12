<template>
  <div
    v-if="active"
    :class="{
      'bg-ladder-bg-promoted text-ladder-text-promoted': !ranker.growing,
      'bg-ladder-bg-you text-ladder-text-you': isYou,
    }"
    class="flex flex-row flex-nowrap justify-between items-center px-1 text-sm"
  >
    <div class="w-full whitespace-nowrap overflow-hidden">
      {{ ranker.rank }} {{ ranker.assholeTag }}
      <sub :class="{ 'text-text-dark': !isYou }">{{
        ranker.assholePoints
      }}</sub>
    </div>
    <div class="w-full whitespace-nowrap overflow-hidden">
      {{ ranker.username
      }}<sub :class="{ 'text-text-dark': !isYou }">#{{ ranker.accountId }}</sub>
    </div>
    <div class="w-full text-right whitespace-nowrap overflow-hidden">
      {{ formattedPowerPerSec }}[<span class="text-eta-best"
        >+{{ formattedBias }}</span
      ><span class="text-eta-worst"> x{{ formattedMulti }}</span
      >]
    </div>
    <div class="w-full text-right whitespace-nowrap overflow-hidden">
      {{ formattedPower }}
    </div>
    <div class="w-full text-right whitespace-nowrap overflow-hidden">
      {{ formattedPoints }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { Ranker } from "~/store/entities/ranker";
import { useFormatter } from "~/composables/useFormatter";

const props = defineProps({
  ranker: { type: Ranker, required: true },
  active: { type: Boolean, required: false, default: true },
  index: { type: Number, required: false, default: -1 },
});

const isYou = computed(() => props.ranker.accountId === 3);

const formattedPowerPerSec = computed<string>(() => {
  if (!props.ranker.growing) return "";
  return `(+${useFormatter(
    (props.ranker.rank + props.ranker.bias) * props.ranker.multi
  )}/s) `;
});

const formattedBias = computed<string>(() => {
  return props.ranker.bias.toString().padStart(2, "0");
});

const formattedMulti = computed<string>(() => {
  return props.ranker.multi.toString().padStart(2, "0");
});

const formattedPower = computed(() => {
  return useFormatter(props.ranker.power);
});
const formattedPoints = computed(() => {
  return useFormatter(props.ranker.points);
});
</script>

<style scoped></style>
