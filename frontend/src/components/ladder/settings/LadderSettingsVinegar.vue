<template>
  <LadderSettingsWithIcon data-tutorial="vinegarSettings" @click="open">
    <font-awesome-icon icon="fa-solid fa-wine-bottle" />
  </LadderSettingsWithIcon>
  <FairDialog
    :open="isOpen"
    :title="'Vinegar Settings'"
    @close="isOpen = false"
  >
    <div class="flex flex-col">
      <p class="w-fit whitespace-nowrap h-full align-middle text-xl pb-2">
        Wine/Vinegar Split:
      </p>
      <div class="flex flex-row justify-between">
        <span class="flex flex-col-reverse"
          >{{ 100 - splitValue }}% x Wine/s</span
        >
        <span class="flex flex-col-reverse">{{ splitValue }}% x Vinegar/s</span>
      </div>
      <input
        type="range"
        :value="splitValue"
        @input="(event) => setSplit(event.target.valueAsNumber)"
      />
      <div class="flex flex-row-reverse justify-between pb-4">
        <FairButton
          :disabled="splitValue === savedVinegarSplit"
          @click="saveVinegarSplit"
          >Save Split</FairButton
        >
        <span>Current: {{ savedWineSplit }}/{{ savedVinegarSplit }}</span>
      </div>

      <p class="w-fit whitespace-nowrap h-full align-middle text-xl pb-2">
        Percentage Thrown:
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
        :value="grapesStore.storage.vinegarThrowPercentage"
        @input="(event) => setVinegarPercentage(event.target.valueAsNumber)"
      />
      <div class="flex flex-row-reverse">
        ({{ grapesStore.storage.vinegarThrowPercentage }}%)
        {{ selectedVinegar }} Vinegar
      </div>
      <p class="w-fit whitespace-nowrap h-full align-middle text-xl pb-2">
        Vinegar-Throws:
      </p>
      <TheVinegarThrowTable />
    </div>
    <br />
    <div class="flex flex-row justify-between">
      <NuxtLink to="/grapes">
        <FairButton>Show Throw-Log</FairButton>
      </NuxtLink>
    </div>
  </FairDialog>
</template>

<script lang="ts" setup>
// FIXME: I18N
import FairDialog from "~/components/interactables/FairDialog.vue";
import FairButton from "~/components/interactables/FairButton.vue";
import { useGrapesStore } from "~/store/grapes";
import { useRoundStore } from "~/store/round";
import { useFormatter } from "~/composables/useFormatter";
import { useAccountStore } from "~/store/account";
import TheVinegarThrowTable from "~/components/grapes/TheVinegarThrowTable.vue";

const isOpen = ref<boolean>(false);

const grapesStore = useGrapesStore();
const accountStore = useAccountStore();
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

const splitValue = ref<number>(50);

const savedVinegarSplit = computed(
  () => accountStore.state.settings.vinegarSplit,
);
const savedWineSplit = computed(
  () => 100 - accountStore.state.settings.vinegarSplit,
);

const selectedVinegar = computed<string>(() =>
  useFormatter(grapesStore.getters.selectedVinegar),
);

function setVinegarPercentage(value: number) {
  if (value < min.value || value > max.value) return;
  grapesStore.storage.vinegarThrowPercentage = value;
}

function setSplit(value: number) {
  if (value < 0 || value > 100) return;
  splitValue.value = value;
}

function saveVinegarSplit() {
  grapesStore.actions.setVinegarSplit(splitValue.value);
}

function open() {
  isOpen.value = true;
  splitValue.value = accountStore.state.settings.vinegarSplit;
}
</script>
