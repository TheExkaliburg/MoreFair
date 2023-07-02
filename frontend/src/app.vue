<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script lang="ts" setup>
/*
- TODO: Check that Reset Logic is actually working

// Pages
- TODO: Mod-Page, and options for mods
- TODO: Theme-Selector and customThemes
- TODO: Add Changelog back

// QoL
- TODO: click to mention in chat should add the mention at the place of the cursor

// Backend
- TODO: Add new Backend-Testcases for authentication (100% coverage)

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
