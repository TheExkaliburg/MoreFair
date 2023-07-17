<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script lang="ts" setup>
/*
- TODO: Check that Reset Logic is actually working


// If im feeling funny but will probably only get done after rework is finished
- TODO: Statistics
- TODO: Overhaul the Vinegar Throw Logic
- TODO: Chat Overhaul (Local + Global)
- TODO: play around with electron, maybe it will work maybe not
 */

import Fair from "~/utils/hooks";

useAPI();

window.Fair = Fair;
</script>
