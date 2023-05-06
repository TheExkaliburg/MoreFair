<template>
  <button
    class="rounded-md border-solid border-1 h-8 px-2 bg-button-bg text-button-text border-button-border disabled:opacity-50 disabled:grayscale disabled:pointer-events-none hover:bg-button-bg-hover hover:text-button-text-hover"
  >
    <slot />
  </button>
</template>

<script lang="ts" setup></script>
