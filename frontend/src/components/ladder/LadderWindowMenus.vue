<template>
  <div class="flex flex-col-reverse">
    <div class="flex flex-row-reverse gap-1">
      <LadderSettingsVinegar />
      <LadderHelp />
    </div>
  </div>
</template>

<script setup lang="ts">
import LadderSettingsVinegar from "~/components/ladder/settings/LadderSettingsVinegar.vue";
import LadderHelp from "~/components/ladder/settings/LadderHelp.vue";
</script>

<style scoped lang="scss"></style>
