<template>
  <span class="whitespace-nowrap overflow-hidden">
    {{ username }}<sub class="text-text-dark">#{{ accountId }}</sub>
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useUserStore } from "~/store/user";

const props = defineProps({
  accountId: { type: Number, required: true },
});

const userStore = useUserStore();

const username = computed<string>(
  () =>
    userStore.getters.getUser(props.accountId)?.displayName ?? "Mystery Guest",
);
</script>
