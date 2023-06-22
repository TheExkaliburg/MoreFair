<template>
  <span
    v-tippy="{ content: description, placement: placement }"
    class="cursor-default"
    >{{ identifier }}</span
  >
</template>

<script lang="ts" setup>
import { LadderType } from "~/store/ladder";
import { RoundType } from "~/store/round";

const props = defineProps<{
  type: LadderType | RoundType;
  placement: "bottom" | "top";
}>();

const lang = useLang("modifier." + props.type);
const identifier = computed<string>(() => {
  return lang("identifier");
});
const description = computed<string>(() => {
  return `${lang("title")}: ${lang("description")}`;
});
</script>

<style lang="scss" scoped></style>
