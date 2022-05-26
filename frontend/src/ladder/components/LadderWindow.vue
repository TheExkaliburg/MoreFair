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
      <span
        >Ladder: {{ store.state.ladder.ladder.ladderNumber }}/{{
          store.state.settings.assholeLadder
        }}</span
      >
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
        <tr v-if="showEtaSetting" class="thead-light">
          <th class="col-1 text-start">#</th>
          <th class="col-3 text-start">Username</th>
          <th class="col-1 text-end">ETA -> L{{ ladder.ladderNumber + 1 }}</th>
          <th class="col-1 text-end">ETA -> You</th>
          <th class="col-3 text-end">Power</th>
          <th class="col-3 text-end">Points</th>
        </tr>
        <tr v-else class="thead-light">
          <th class="col-1 text-start">#</th>
          <th class="col-5 text-start">Username</th>
          <th class="col-3 text-end">Power</th>
          <th class="col-3 text-end">Points</th>
        </tr>
      </thead>
      <tbody id="ladderBody" class="">
        <tr
          v-for="ranker in shownRankers"
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
          <td class="text-start">
            {{ ranker.username }}
            <sub class="account-id">#{{ ranker.accountId }}</sub>
          </td>
          <td
            v-if="showEtaSetting"
            :style="'animation-delay: ' + rankerEtaPercentage(ranker) + 's'"
            class="text-end etaProgressAnim"
          >
            {{ secondsToHms(eta(ranker).toPromote()) }}
          </td>
          <td
            v-if="showEtaSetting"
            :style="'animation-delay: ' + rankerEtaPercentage(ranker) + 's'"
            class="text-end etaProgressAnim"
          >
            {{ secondsToHms(eta(ranker).toRanker(yourRanker)) }}
          </td>
          <td class="text-end">
            {{ numberFormatter.format(ranker.power) }} [+{{
              ("" + ranker.bias).padStart(2, "0")
            }}
            x{{ ("" + ranker.multiplier).padStart(2, "0") }}]
          </td>
          <td
            :style="'animation-delay: ' + rankerEtaPercentage(ranker) + 's'"
            class="text-end etaProgressAnim"
          >
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
import { eta } from "@/modules/eta";
import { secondsToHms } from "@/modules/formatting";

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
const showEtaSetting = computed(() =>
  store.getters["options/getOptionValue"]("showETA")
);
const hidePromotedPlayers = computed(() =>
  store.getters["options/getOptionValue"]("hidePromotedPlayers")
);
const shownRankers = computed(() => {
  if (hidePromotedPlayers.value) {
    return rankers.value.filter((ranker) => ranker.growing || ranker.you);
  } else {
    return rankers.value;
  }
});

// should return the value from fastest (0%) to as long as it takes for the top (50%) to double as long (100%)
// as a negative, because the animation-delay only sets the start value if the delay is negative, otherwise it's an actual delay
function rankerEtaPercentage(ranker) {
  if (etaColorSetting.value === "Off") {
    return 1;
  }
  if (ranker.you) {
    return 1;
  }
  if (!ranker.growing) {
    return 1;
  }

  const etaToRanker = eta(ranker).toRanker(yourRanker.value);
  const youEtaToFirst = eta(yourRanker.value).toFirst();

  // we want to return a percentage for our animation interpolation
  // 0 is to overtake now
  // 50 is eta to overtake equals eta to first
  // 100 is eta to overtake equals eta to first * 2
  let gradientPercent = (etaToRanker / youEtaToFirst) * 50;
  gradientPercent = Math.min(Math.max(gradientPercent, 0), 100);

  //check if the ranker is behind us
  if (ranker.rank > yourRanker.value.rank) {
    // we want to return a percentage for our animation interpolation
    // 0 is eta to overtake equals eta to first * 2
    // 50 is eta to overtake equals eta to first
    // 100 is 0 seconds to overtake
    gradientPercent = 100 - gradientPercent;
  }

  if (etaColorSetting.value === "3-Color") {
    if (gradientPercent < 45) {
      gradientPercent = 0;
    } else if (gradientPercent < 55) {
      gradientPercent = 50;
    } else {
      gradientPercent = 100;
    }
  }

  return -gradientPercent;
}

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

.account-id {
  color: var(--text-dark-highlight-color);
}

.dropdown-pagination {
  text-align: end;
  padding-right: 0px;
}

.you {
  background-color: var(--you-background-color);
  color: var(--you-color);

  .account-id {
    color: var(--you-color);
  }
}

.promoted {
  background-color: var(--promoted-background-color);
  color: var(--promoted-color);
}

@keyframes etaProgress {
  0% {
    color: var(--eta-best);
  }
  50% {
    color: var(--eta-mid);
  }
  100% {
    color: var(--eta-worst);
  }
}

.etaProgressAnim {
  // The Animation moves through the keyframes but is paused,
  // so only the negative delay can change anything for it
  animation: 101s linear paused etaProgress;
}
</style>
