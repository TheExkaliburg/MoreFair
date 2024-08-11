<template>
  <table>
    <tr>
      <th>Thrower</th>
      <th>Target</th>
      <th>Vinegar</th>
      <th>Success</th>
    </tr>
    <tr v-for="(vinThrow, index) in shownThrows" :key="index">
      <td><UserLabel :account-id="vinThrow.accountId" /></td>
      <td><UserLabel :account-id="vinThrow.targetId" /></td>
      <td>{{ vinThrow.vinegarThrown }} ({{ vinThrow.percentage }}%)</td>
      <td>{{ vinThrow.successType }}</td>
    </tr>
  </table>
</template>

<script setup lang="ts">
import UserLabel from "~/components/core/UserLabel.vue";
import { useGrapesStore, VinegarThrow } from "~/store/grapes";

const grapesStore = useGrapesStore();

const shownThrows = computed<VinegarThrow[]>(() =>
  grapesStore.state.vinegarThrowLog.slice(
    0,
    Math.min(grapesStore.state.vinegarThrowLog.length, 10),
  ),
);
</script>
