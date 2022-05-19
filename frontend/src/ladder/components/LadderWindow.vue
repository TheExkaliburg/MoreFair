<template>
  <div class="row py-1">
    <div class="col">
      <span
        >Active Rankers: {{ store.getters["ladder/activeRankers"].length }}/{{
          store.state.ladder.ladder.rankers.length
        }}</span
      >
    </div>
    <div class="col">
      <span>Asshole Ladder: {{ store.state.settings.assholeLadder }}</span>
    </div>
    <PaginationGroup
      :current="ladder.ladderNumber"
      :max="
        store.getters['options/getOptionValue']('enableUnrestrictedAccess') &&
        store.getters.isMod
          ? Math.max(settings.assholeLadder, user.highestCurrentLadder)
          : user.highestCurrentLadder
      "
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
          <td class="text-end" :style="[rankerEtaColor(ranker)]">
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
import { eta } from "../../modules/eta";
import PaginationGroup from "@/components/PaginationGroup";

const store = useStore();
const stompClient = inject("$stompClient");

const numberFormatter = computed(() => store.state.numberFormatter);
const ladder = computed(() => store.state.ladder.ladder);
const user = computed(() => store.state.user);
const settings = computed(() => store.state.settings);
const rankers = computed(() => store.getters["ladder/shownRankers"]);
const yourRanker = computed(() => ladder.value.yourRanker);
const etaColorSetting = computed(() =>
  store.getters["options/getOptionValue"]("etaColors")
);

const rankerEtaColor = (ranker) => {
  if (etaColorSetting.value === "Off") {
    return {};
  }
  if (etaColorSetting.value === "3-Color") {
    return rankerEtaClass(ranker);
  }
  return rankerEtaPoints(ranker);
};

const rankerEtaClass = (ranker) => {
  if (ranker.you) {
    return {};
  }
  if (!ranker.growing) {
    return {};
  }

  const theirETAToFirst = eta(ranker).toFirst();
  const yourETAToFirst = eta(yourRanker.value).toFirst();

  if (!ranker.growing) {
    return {};
  }

  if (theirETAToFirst < yourETAToFirst + 29) {
    if (theirETAToFirst < yourETAToFirst) {
      return { color: "var(--eta-worst)" }; //they will be first to first
    }
    return { color: "var(--eta-mid)" }; //they will be second to first but will overtake you befor the 30 second mark for manual promote
  }
  if (yourETAToFirst < theirETAToFirst) {
    if (yourETAToFirst + 31 < theirETAToFirst) {
      return { color: "var(--eta-best)" }; //you will be first to first and have time to manually promote
    }
    return { color: "var(--eta-mid)" }; //they will be second to first but will overtake you befor the 30 second mark for manual promote
  }
  return {};
};

const rankerEtaPoints = (ranker) => {
  if (ranker.you) {
    return {};
  }
  if (!ranker.growing) {
    return {};
  }

  const etaToRanker = eta(ranker).toRanker(yourRanker.value);
  const youEtaToFirst = eta(yourRanker.value).toFirst();
  //  const theirEtaToFirst = eta(ranker).toFirst();

  //check if the ranker is in front of us
  if (ranker.rank < yourRanker.value.rank) {
    //we want to return a color on a hsl scale
    //hsl scale is green over orange to red
    //green is 0 seconds to overtake
    //orange is eta to overtake equals eta to first
    //red is eta to overtake equals eta to first * 2

    let gradientPercent = (etaToRanker / youEtaToFirst) * 50;

    gradientPercent = Math.min(Math.max(gradientPercent, 0), 100);

    //return a color in hsl format
    return {
      color: `hsl(${100 - gradientPercent}, 100%, 50%)`,
    };
  }
  //we want to return a color on a hsl scale
  //hsl scale is green over orange to red
  //green is eta to overtake equals eta to first
  //red is 0 seconds to overtake

  let gradientPercent = (etaToRanker / youEtaToFirst) * 100;

  gradientPercent = Math.min(Math.max(gradientPercent, 0), 100);

  //return a color in hsl format
  return {
    color: `hsl(${gradientPercent}, 100%, 50%)`,
  };
};

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
    background-color: var(--background-color);
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
  background-color: var(--you-background-color);
  color: var(--you-color);
}

.promoted {
  background-color: var(--promoted-background-color);
  color: var(--promoted-color);
}
</style>
