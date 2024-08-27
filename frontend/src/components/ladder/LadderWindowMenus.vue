<template>
  <div class="flex flex-col-reverse">
    <div class="flex flex-row-reverse gap-1">
      <LadderSettingsVinegar :disabled="!flags.shownVinegar" />
      <LadderHelp />
    </div>
  </div>
</template>

<script setup lang="ts">
import LadderSettingsVinegar from "~/components/ladder/settings/LadderSettingsVinegar.vue";
import LadderHelp from "~/components/ladder/settings/LadderHelp.vue";

const flags = useStartupTour().flags;
</script>

<style scoped lang="scss"></style>
