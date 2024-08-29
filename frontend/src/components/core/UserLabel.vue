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

const user = userStore.getters.getUser(props.accountId);
const username = computed<string>(
  () => user.value?.displayName ?? "Mystery Guest",
);
</script>
