<template>
  <table
    class="table table-sm caption-top table-borderless"
    style="border: 0px solid yellow"
  >
    <thead>
      <tr class="thead-light">
        <th class="col-1">#</th>
        <th class="col-5">Username</th>
        <th class="col-3 text-end">Power</th>
        <th class="col-3 text-end">Points</th>
      </tr>
    </thead>
    <tbody id="ladderBody" class="">
      <tr v-for="ranker in ladder.rankers" :key="ranker">
        <td>{{ ranker.rank }}</td>
        <td class="text-start">{{ ranker.username }}</td>
        <td class="text-end">
          {{ numberFormatter.format(ranker.power) }}
        </td>
        <td class="text-end">
          {{ numberFormatter.format(ranker.points) }}
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
import { useStore } from "vuex";
import { computed } from "vue";

const store = useStore();

const numberFormatter = computed(() => store.state.numberFormatter);
const ladder = computed(() => store.state.ladder.ladder);
</script>

<style scoped>
.right-aligned-cell {
  text-align: right;
}

table tbody {
  height: calc(50% - 85px) !important;
  min-height: calc(50% - 85px) !important;
  max-height: calc(50% - 85px) !important;
}
</style>
