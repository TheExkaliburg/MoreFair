<template>
  <div class="flex flex-col justify-around text-md lg:text-xl">
    <div class="flex flex-row justify-center items-center relative w-full">
      <div
        class="w-full rounded-l-md border-1 border-button-border p-1 outline-0 overflow-x-hidden text-text"
        contenteditable
        role="textbox"
        spellcheck="false"
        type="text"
      ></div>
      <div
        class="absolute left-1 pointer-events-none p-1 text-text-placeholder"
      >
        Chat is listening...
      </div>
      <button
        class="w-1/4 rounded-r-md border-l-0 border-1 border-button-border py-1 text-button-text hover:text-button-text-hover hover:bg-button-bg-hover"
      >
        Send
      </button>
    </div>
    <div class="text-xs lg:text-xs text-left w-full px-2 text-text-light">
      Message length: 0 / 280
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useDomUtils } from "~/composables/useDomUtils";

useDomUtils();
</script>
