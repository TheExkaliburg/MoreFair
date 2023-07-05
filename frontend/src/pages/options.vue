<template>
  <div class="flex flex-row w-full justify-evenly text-text mt-12">
    <div class="flex flex-col w-full max-w-reader space-y-2 px-2 text-sm">
      <template v-for="entry in currentOptionsArray" :key="entry.key">
        <OptionsSection
          v-if="entry.value instanceof OptionsGroup"
          :label="entry.key"
          :options="entry.value"
        />
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import OptionsSection from "../components/options/OptionsSection.vue";
import { useOptionsStore } from "~/store/options";
import { OptionsGroup } from "~/store/entities/option";

useSeoMeta({
  title: "Options",
  description: "Change your options",
});

const optionsStore = useOptionsStore();

const currentOptionsArray = computed(() => {
  return Object.entries(optionsStore.state).map(([key, value]) => {
    return {
      key,
      value,
    };
  });
});
</script>

<style lang="scss"></style>
