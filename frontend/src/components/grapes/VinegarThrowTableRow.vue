<template>
  <tr>
    <td>R{{ typedVinThrow.roundNumber }}L{{ typedVinThrow.ladderNumber }}</td>
    <td>{{ timestampString }}</td>
    <td><UserLabel :account-id="typedVinThrow.accountId" /></td>
    <td><UserLabel :account-id="typedVinThrow.targetId" /></td>
    <td class="text-right">
      ({{ typedVinThrow.percentage }}%) {{ typedVinThrow.vinegarThrown }}
    </td>
    <td class="text-right">{{ typedVinThrow.successType }}</td>
  </tr>
</template>
<script setup lang="ts">
import UserLabel from "~/components/core/UserLabel.vue";
import { VinegarThrow } from "~/store/grapes";
import { useDateFormatter } from "~/composables/useFormatter";

const props = defineProps({
  // TODO: Fix this in Vue Version 3.3
  vinThrow: { type: Object, required: true },
});

const typedVinThrow = computed<VinegarThrow>(
  () => props.vinThrow as VinegarThrow,
);

const timestampString = computed<string>(() =>
  useDateFormatter(typedVinThrow.value.timestamp, {
    day: "numeric",
    month: "numeric",
    hour: "numeric",
    minute: "numeric",
    hour12: false,
  }),
);
</script>
