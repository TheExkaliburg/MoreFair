<template>
  <div
    v-if="active"
    :class="{
      'bg-ladder-bg-promoted text-ladder-text-promoted': !ranker.growing,
      'bg-ladder-bg-you text-ladder-text-you': isYou,
      'sticky top-0 z-1': isFirst,
    }"
    class="grid grid-cols-24 sm:grid-cols-48 gap-1 px-1 text-sm select-none"
  >
    <div class="col-span-3 whitespace-nowrap overflow-hidden">
      {{ ranker.rank }} {{ ranker.assholeTag }}
      <sub :class="{ 'text-text-dark': !isYou }">{{
        ranker.assholePoints
      }}</sub>
    </div>
    <div class="col-span-9 whitespace-nowrap overflow-hidden">
      {{ ranker.username
      }}<sub :class="{ 'text-text-dark': !isYou }">#{{ ranker.accountId }}</sub>
    </div>

    <div
      class="col-span-6 text-right whitespace-nowrap overflow-hidden sm:order-last"
    >
      {{ formattedPower }}
    </div>
    <div
      class="col-span-6 text-right whitespace-nowrap overflow-hidden sm:order-last"
    >
      {{ formattedPoints }}
    </div>
    <div class="col-span-7 text-right whitespace-nowrap overflow-hidden">
      100:52:07
    </div>
    <div class="col-span-7 text-right whitespace-nowrap overflow-hidden">
      1:04:12
    </div>
    <div class="col-span-10 text-right whitespace-nowrap overflow-hidden">
      {{ formattedPowerPerSec }}[<span class="text-eta-best"
        >+{{ formattedBias }}</span
      ><span class="text-eta-worst"> x{{ formattedMulti }}</span
      >]
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

const isFirst = computed(() => {
  return props.index === 0;
});
</script>

<style scoped></style>
