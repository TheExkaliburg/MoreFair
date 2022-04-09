<template>
  <div class="row py-1">
    <PaginationGroup
      :current="ladder.ladderNumber"
      :max="user.highestCurrentLadder"
      :onChange="changeLadder"
    />
  </div>
  <div class="row py-1 ladder-row">
    <table
      class="table table-sm caption-top table-borderless"
      style="border: 0px solid yellow"
    >
      <thead>
        <tr class="thead-light">
          <th class="col-1 text-start">#</th>
          <th class="col-5 text-start">Username</th>
          <th class="col-3 text-end">Power</th>
          <th class="col-3 text-end">Points</th>
        </tr>
      </thead>
      <tbody id="ladderBody" class="">
        <tr
          v-for="ranker in rankers"
          :key="ranker.accountId"
          :class="[
            ranker.you ? 'you' : '',
            ranker.growing || ranker.you ? '' : 'promoted',
          ]"
        >
          <td class="text-start">
            {{ ranker.rank }}
            {{ settings.assholeTags[ranker.timesAsshole] }}
          </td>
          <td class="text-start">{{ ranker.username }}</td>
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
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject } from "vue";
import PaginationGroup from "@/components/PaginationGroup";

const store = useStore();
const stompClient = inject("$stompClient");

const numberFormatter = computed(() => store.state.numberFormatter);
const ladder = computed(() => store.state.ladder.ladder);
const user = computed(() => store.state.user);
const settings = computed(() => store.state.settings);
const rankers = computed(() => store.getters["ladder/shownRankers"]);

function changeLadder(event) {
  const targetLadder = event.target.dataset.number;

  if (targetLadder !== ladder.value.ladderNumber) {
    stompClient.unsubscribe("/topic/ladder/" + ladder.value.ladderNumber);
    stompClient.subscribe("/topic/ladder/" + targetLadder, (message) => {
      store.dispatch({
        type: "ladder/update",
        message: message,
        stompClient: stompClient,
      });
    });
    stompClient.send("/app/ladder/init/" + targetLadder);
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/styles";

.rank {
  padding-left: 1rem;
}

.ladder-row {
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

.dropdown-pagination {
  text-align: end;
  padding-right: 0px;
}

.you {
  background-color: $button-color;
  color: $button-hover-color;
}

.promoted {
  background-color: #1e1d39;
}
</style>
