<template>
  <div class="flex flex-row justify-between items-center text-button-text">
    <ExtendedInformationModal
      class="sm:hidden px-1 w-8"
      title="Ladder Information"
    >
      <template #button><InformationCircleIcon /></template>
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
        <div class="whitespace-nowrap">
          Round: [{{ roundStore.getters.formattedTypes }}]
        </div>
        <div class="whitespace-break-spaces">
          Ladder: [{{ ladderStore.getters.formattedTypes }}]
        </div>
      </div>
    </ExtendedInformationModal>
    <div class="-sm:hidden grid grid-cols-2 text-xs pl-8">
      <div class="whitespace-nowrap">
        Ladders: {{ accountStore.state.highestCurrentLadder }}/{{
          roundStore.state.assholeLadder
        }}
        ({{ roundStore.state.topLadder }})
      </div>
      <div class="whitespace-nowrap">
        Round: [{{ roundStore.getters.formattedTypes }}]
      </div>
      <div class="whitespace-nowrap">
        Active Rankers: {{ ladderStore.getters.activeRankers }}/{{
          ladderStore.state.rankers.length
        }}
      </div>
      <div class="whitespace-nowrap">
        Ladder: [{{ ladderStore.getters.formattedTypes }}]
      </div>
    </div>
    <PaginationButtonGroup
      :current="ladderStore.state.number"
      :max="accountStore.state.highestCurrentLadder"
      :prefix="'Ladder'"
      class="h-8 w-42 self-end bg-background z-2 text-base"
      :last="roundStore.state.assholeLadder"
      :show-last="isAssholeLadderOpen"
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
import ExtendedInformationModal from "~/components/interactables/ExtendedInformationModal.vue";

const ladderStore = useLadderStore();
const roundStore = useRoundStore();
const accountStore = useAccountStore();

const isAssholeLadderOpen = computed<boolean>(
  () => roundStore.state.topLadder >= roundStore.state.assholeLadder
);
</script>
