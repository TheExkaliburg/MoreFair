<template>
  <LadderSettingsWithIcon @click="isOpen = true">
    <font-awesome-icon icon="fa-solid fa-wine-bottle" />
  </LadderSettingsWithIcon>
  <FairDialog
    :open="isOpen"
    :title="'Vinegar Settings'"
    @close="isOpen = false"
  >
    <div class="flex flex-col">
      <p class="w-fit whitespace-nowrap h-full align-middle text-xl pb-2">
        Throwing Amount (Placeholder):
      </p>
      <div class="flex flex-row justify-between">
        <span class="flex flex-col-reverse">{{ min }}%</span>
        <div class="flex flex-row">
          <FairButton
            class="w-1/3 pr-4 text-center border-r-0 rounded-r-none"
            @click="setVinegarPercentage(min)"
            >{{ min }}%
          </FairButton>
          <FairButton
            class="w-1/3 pr-4 text-center border-r-0 rounded-none"
            @click="setVinegarPercentage(middle)"
          >
            {{ middle }}%
          </FairButton>
          <FairButton
            class="w-1/3 pr-4 text-center rounded-l-none"
            @click="setVinegarPercentage(max)"
            >{{ max }}%
          </FairButton>
        </div>
        <span class="flex flex-col-reverse">{{ max }}%</span>
      </div>
      <input
        type="range"
        :max="max"
        :min="min"
        :value="grapesStore.state.vinegarThrowPercentage"
        @input="(event) => setVinegarPercentage(event.target.valueAsNumber)"
      />
      <div class="flex flex-row-reverse">
        ({{ grapesStore.state.vinegarThrowPercentage }}%) {{ selectedVinegar }}
      </div>
    </div>
  </FairDialog>
</template>

<script lang="ts" setup>
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useGrapesStore } from "~/store/grapes";
import { useRoundStore } from "~/store/round";
import { useFormatter } from "~/composables/useFormatter";

const isOpen = ref<boolean>(false);

const grapesStore = useGrapesStore();
const roundStore = useRoundStore();

const min = computed(() =>
  roundStore.state.settings.minVinegarThrown < 0
    ? 0
    : roundStore.state.settings.minVinegarThrown,
);
const max = computed(() =>
  roundStore.state.settings.maxVinegarThrown > 100
    ? 100
    : roundStore.state.settings.maxVinegarThrown,
);
const middle = computed(() => Math.round((max.value + min.value) / 2));

const selectedVinegar = computed<string>(() =>
  useFormatter(grapesStore.getters.selectedVinegar),
);

function setVinegarPercentage(value: number) {
  if (value < min.value || value > max.value) return;
  grapesStore.state.vinegarThrowPercentage = value;
}
</script>
