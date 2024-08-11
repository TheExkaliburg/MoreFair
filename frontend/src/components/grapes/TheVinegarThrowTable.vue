<template>
  <table>
    <tr>
      <th>Ladder</th>
      <th>Timestamp</th>
      <th>Thrower</th>
      <th>Target</th>
      <th>Vinegar</th>
      <th>Success</th>
    </tr>
    <VinegarThrowTableRow
      v-for="(vinThrow, index) in shownThrows"
      :key="index"
      :vin-throw="vinThrow"
    />
  </table>
</template>

<script setup lang="ts">
import { useGrapesStore, VinegarThrow } from "~/store/grapes";
import VinegarThrowTableRow from "~/components/grapes/VinegarThrowTableRow.vue";

const grapesStore = useGrapesStore();

const shownThrows = computed<VinegarThrow[]>(() =>
  grapesStore.state.throwRecords.slice(
    0,
    Math.min(grapesStore.state.throwRecords.length, 10),
  ),
);
</script>
