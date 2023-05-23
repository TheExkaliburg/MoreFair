<template>
  <div class="flex flex-row justify-between items-center text-button-text">
    <div class="flex flex-row sm:gap-6">
      <ExtendedInformationModal
        aria-label="ladder information"
        class="px-1 w-8 xl:invisible"
        title="Ladder Information"
      >
        <template #button>
          <InformationCircleIcon />
        </template>
        <div class="text-sm">
          <div class="whitespace-nowrap">
            Active Rankers: {{ ladderStore.getters.activeRankers }}/{{
              ladderStore.state.rankers.length
            }}
          </div>
          <div class="whitespace-nowrap">
            Ladders: {{ accountStore.state.highestCurrentLadder }}/{{
              roundStore.state.assholeLadder
            }}
            ({{ roundStore.state.topLadder }})
          </div>
          <br />
          <div class="whitespace-nowrap">
            Round:
            <TypeInformation
              :types="roundStore.state.types"
              placement="bottom"
            />
          </div>
          <div class="whitespace-break-spaces">
            Ladder:
            <TypeInformation
              :types="ladderStore.state.types"
              placement="bottom"
            />
          </div>
          <br />
          <div class="whitespace-nowrap">
            Base Points for Promotion: {{ formattedRoundPointsForPromotion }}
          </div>
          <div class="whitespace-nowrap">
            Points for Promotion: {{ formattedPointsForPromotion }}
          </div>
        </div>
      </ExtendedInformationModal>
      <div class="flex flex-row gap-6 text-xs">
        <div class="flex flex-col">
          <div class="whitespace-nowrap" data-tutorial="info">
            Ladders: {{ accountStore.state.highestCurrentLadder }}/{{
              roundStore.state.assholeLadder
            }}
            ({{ roundStore.state.topLadder }})
          </div>
          <div class="whitespace-nowrap">
            Active Rankers: {{ ladderStore.getters.activeRankers }}/{{
              ladderStore.state.rankers.length
            }}
          </div>
        </div>
        <div class="flex flex-col -sm:hidden">
          <div class="whitespace-nowrap">
            Round:
            <TypeInformation
              :types="roundStore.state.types"
              placement="bottom"
            />
          </div>
          <div class="whitespace-nowrap">
            Ladder:
            <TypeInformation
              :types="ladderStore.state.types"
              placement="bottom"
            />
          </div>
        </div>
        <div class="flex flex-col -xl:hidden">
          <div class="whitespace-nowrap">
            Base Points for Promotion: {{ formattedRoundPointsForPromotion }}
          </div>
          <div class="whitespace-nowrap">
            Points for Promotion: {{ formattedPointsForPromotion }}
          </div>
        </div>
      </div>
    </div>
    <PaginationButtonGroup
      :current="ladderStore.state.number"
      :last="roundStore.state.assholeLadder"
      :max="accountStore.state.highestCurrentLadder"
      :prefix="'Ladder'"
      :show-last="isAssholeLadderOpen"
      class="h-8 w-42 self-end bg-background z-2 text-base"
      @change="(number) => ladderStore.actions.changeLadder(number)"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { InformationCircleIcon } from "@heroicons/vue/24/outline";
import PaginationButtonGroup from "../../components/interactables/PaginationButtonGroup.vue";
import { useLadderStore } from "~/store/ladder";
import { useAccountStore } from "~/store/account";
import { useRoundStore } from "~/store/round";
import ExtendedInformationModal from "~/components/interactables/ExtendedInformationDialog.vue";
import { useFormatter } from "~/composables/useFormatter";
import TypeInformation from "~/components/ladder/TypeInformation.vue";

const ladderStore = useLadderStore();
const roundStore = useRoundStore();
const accountStore = useAccountStore();

const formattedRoundPointsForPromotion = computed<string>(() => {
  return useFormatter(roundStore.state.settings.basePointsForPromote);
});

const formattedPointsForPromotion = computed<string>(() => {
  return useFormatter(ladderStore.state.basePointsToPromote);
});

const isAssholeLadderOpen = computed<boolean>(
  () => roundStore.state.topLadder >= roundStore.state.assholeLadder
);
</script>
