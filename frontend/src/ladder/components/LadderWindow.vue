<template>
  <div
    style="overflow: hidden; max-height: calc(100% - 150px); overflow-y: auto"
  >
    <div class="row py-1 ladder-row">
      <table
        class="table table-sm caption-top table-borderless"
        style="border: 0px solid yellow"
      >
        <thead>
          <tr class="thead-light">
            <th class="col-1 text-start">-</th>
            <th class="col-5 text-start">Username</th>
            <th class="col-3 text-end">Power</th>
            <th class="col-3 text-end">Points</th>
          </tr>
        </thead>
        <tbody id="ladderBody" class="">
          <tr
            v-for="ranker in rankers"
            :key="ranker.accountId"
            :class="[ranker.you ? 'you' : '', ranker.growing ? '' : 'promoted']"
          >
            <td class="text-start">{{ ranker.rank }}</td>
            <td class="text-start">{{ ranker.username }} {{ ranker.rank }}</td>
            <td class="text-end">
              {{ numberFormatter.format(ranker.power) }} [+{{
                ("" + ranker.bias).padStart(2, "0")
              }}
              x{{ ("" + ranker.multiplier).padStart(2, "0") }}]
            </td>
            <td class="text-end">
              {{ numberFormatter.format(ranker.points) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed } from "vue";

const store = useStore();

const numberFormatter = computed(() => store.state.numberFormatter);
//const ladder = computed(() => store.state.ladder);
const rankers = computed(() => store.getters["ladder/shownRankers"]);
</script>

<style lang="scss" scoped>
@import "../../styles/styles";

.ladder-row {
  width: 100%;
  height: 50%;
  max-height: 50%;
  overflow-y: auto;
  align-content: start;

  thead {
    background-color: $background-color;
    position: sticky;
    top: 0;
  }

  tbody {
    overflow: auto;
  }
}

.you {
  background-color: $button-color;
  color: $button-hover-color;
}

.promoted {
  background-color: #1e1d39;
}
</style>
