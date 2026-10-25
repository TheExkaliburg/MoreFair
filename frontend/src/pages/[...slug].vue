<template>
  <main
    class="px-4 flex flex-col items-center h-full overflow-y-scroll md-content"
  >
    <div v-if="status === 'pending'" class="text-text">Loading...</div>
    <ContentRenderer
      v-else-if="page"
      :value="page"
      class="text-text max-w-full w-reader"
    />
    <div v-else class="text-text text-3xl">
      404: Cannot find the page you are looking for.
    </div>
    <div class="h-10 select-none">
      &nbsp;<br />&nbsp;<br />&nbsp;<br />&nbsp;
    </div>
  </main>
</template>
<script lang="ts" setup>
const route = useRoute();
const {
  data: page,
  status,
  error,
} = await useAsyncData(
  () => `content:${route.path}`,
  () => queryCollection("pages").path(route.path).first(),
);

watchEffect(() => {
  if (error.value) {
    showError(error.value);
  }
});

useSeoMeta({
  title: () => page.value?.title,
  description: () => page.value?.description,
});
</script>

<style lang="scss" scoped></style>
