<template>
  <div>
    {{ username }}<sub class="">{{ accountId }}</sub>
  </div>
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
