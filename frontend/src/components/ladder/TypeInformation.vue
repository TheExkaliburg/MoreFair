<template>
  <span
    >[<template v-for="(t, i) in typeArray" :key="t"
      ><TypeInformationPart :placement="placement" :type="t" /><span
        v-if="i !== typeArray.length - 1"
        >,
      </span></template
    >]</span
  >
</template>

<script lang="ts" setup>
import { LadderType } from "~/store/ladder";
import { RoundType, useRoundStore } from "~/store/round";
import TypeInformationPart from "~/components/ladder/TypeInformationPart.vue";

const props = defineProps<{
  types: Set<LadderType> | Set<RoundType>;
  placement: "top" | "bottom";
}>();

const roundStore = useRoundStore();

const typeArray = computed<LadderType[] | RoundType[]>(() => {
  if (roundStore.state.types.has(RoundType.APRIL_FOOLS)) {
    return [RoundType.APRIL_FOOLS];
  }

  return Array.from<LadderType | RoundType>(props.types) as
    | LadderType[]
    | RoundType[];
});
</script>

<style lang="scss" scoped></style>
