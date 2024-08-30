<template>
  <div class="w-full">
    <table class="w-full">
      <tr class="text-text-light">
        <th class="text-left">
          {{ abbreviation("round")
          }}<span class="text-sm text-text-dark">x</span
          >{{ abbreviation("ladder")
          }}<span class="text-sm text-text-dark">x</span>
        </th>
        <th class="text-left">{{ lang("time") }}</th>
        <th class="text-left">{{ lang("users") }}</th>
        <th class="text-right">{{ lang("amount") }}</th>
        <th class="text-right">{{ lang("result") }}</th>
      </tr>
      <VinegarThrowTableRow
        v-for="(vinThrow, index) in shownThrows"
        :key="index"
        :vin-throw="vinThrow"
        class="border-text"
        :class="{ 'border-b-1': index < shownThrows.length - 1 }"
      />
    </table>
  </div>
</template>

<script setup lang="ts">
import { useGrapesStore, VinegarThrow } from "~/store/grapes";
import VinegarThrowTableRow from "~/components/grapes/VinegarThrowTableRow.vue";

const grapesStore = useGrapesStore();
const abbreviation = useLang("abbreviations");
const lang = useLang("components.vinegarThrowTable");

const props = defineProps({
  rows: { type: Number, default: 3 },
});

const shownThrows = computed<VinegarThrow[]>(() =>
  grapesStore.state.throwRecords.slice(
    0,
    Math.min(grapesStore.state.throwRecords.length, props.rows),
  ),
);
</script>
