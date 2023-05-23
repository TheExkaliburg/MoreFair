<template>
  <span v-tippy="{ content: description, placement: placement }">{{
    identifier
  }}</span>
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
  return lang("short");
});
const description = computed<string>(() => {
  return `${lang("title")}: ${lang("description")}`;
});
</script>

<style lang="scss" scoped></style>
