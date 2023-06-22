<template>
  <main
    class="px-4 flex flex-col items-center h-full overflow-y-scroll md-content"
  >
    <ContentDoc class="text-text max-w-full w-reader">
      <template #not-found>
        <div class="text-text text-3xl">
          404: Cannot find the page you are looking for.
        </div>
      </template>
    </ContentDoc>
    <div class="h-10 select-none">
      &nbsp;<br />&nbsp;<br />&nbsp;<br />&nbsp;
    </div>
  </main>
</template>
<script lang="ts" setup></script>

<style lang="scss" scoped></style>
