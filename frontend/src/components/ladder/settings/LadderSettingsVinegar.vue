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
      <div
        class="flex flex-row justify-between whitespace-nowrap overflow-hidden"
      >
        <span class="flex flex-col-reverse">
          {{ formattedWinePerSec }} Wine/s ({{ 100 - splitValue }}%)</span
        >
        <span class="flex flex-col-reverse"
          >({{ splitValue }}%) {{ formattedVinegarPerSec }} x Vinegar/s
        </span>
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
        <span>{{ formattedWineShieldEta }}</span>
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
import {
  NumberFormatterType,
  useFormatter,
  useTimeFormatter,
} from "~/composables/useFormatter";
import { useAccountStore } from "~/store/account";
import TheVinegarThrowTable from "~/components/grapes/TheVinegarThrowTable.vue";
import { useLadderStore } from "~/store/ladder";
import { Ranker } from "~/store/entities/ranker";

const isOpen = ref<boolean>(false);

const grapesStore = useGrapesStore();
const accountStore = useAccountStore();
const roundStore = useRoundStore();
const ladderStore = useLadderStore();

const yourRanker = computed(
  () => ladderStore.getters.yourRanker ?? new Ranker({}),
);

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

const winePerSec = computed(() =>
  yourRanker.value.grapes.mul(100 - splitValue.value).div(50),
);
const vinegarPerSec = computed(() =>
  yourRanker.value.grapes.mul(splitValue.value).div(100),
);

const formattedWinePerSec = computed(() =>
  useFormatter(winePerSec.value, formattingSize.value),
);
const formattedVinegarPerSec = computed(() =>
  useFormatter(vinegarPerSec.value, formattingSize.value),
);

const formattingSize = computed(() => {
  if (useWindowSize().isSmallerThanSm) {
    return NumberFormatterType.SMALL;
  } else {
    return NumberFormatterType.DEFAULT;
  }
});

const savedVinegarSplit = computed(
  () => accountStore.state.settings.vinegarSplit,
);
const savedWineSplit = computed(
  () => 100 - accountStore.state.settings.vinegarSplit,
);

const formattedWineShieldEta = computed<string>(() => {
  const wineDeltaPerSec = winePerSec.value.sub(vinegarPerSec.value);
  const wineMissing = yourRanker.value.vinegar.sub(yourRanker.value.wine);
  const seconds = wineMissing.div(wineDeltaPerSec).ceil();
  const formattedSeconds = useTimeFormatter(seconds.toNumber());
  if (wineDeltaPerSec.cmp(0) > 0 && wineMissing.cmp(0) > 0) {
    // Loose more Wine; missing Wine > 0
    return `${formattedSeconds} till Shielded`;
  } else if (wineDeltaPerSec.cmp(0) < 0 && wineMissing.cmp(0) < 0) {
    return `${formattedSeconds} till Unshielded`;
  } else if (wineDeltaPerSec.cmp(0) > 0 && wineMissing.cmp(0) < 0) {
    return "Stay Shielded";
  } else {
    return "Stay Unshielded";
  }
});

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
