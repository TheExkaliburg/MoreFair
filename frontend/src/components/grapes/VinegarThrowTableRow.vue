<template>
  <tr>
    <td>
      {{ abbr("round") }}{{ typedVinThrow.roundNumber }}{{ abbr("ladder")
      }}{{ typedVinThrow.ladderNumber }}
    </td>
    <td>
      <span class="whitespace-nowrap">{{ timestampDateString }}</span>
      <p />
      <span class="whitespace-nowrap">{{ timestampTimeString }}</span>
    </td>
    <td>
      <UserLabel :account-id="typedVinThrow.accountId" />
      <p />
      <UserLabel :account-id="typedVinThrow.targetId" />
    </td>
    <td class="text-right">{{ vinegarString }}</td>
    <td v-tippy="{ content: successTypeTooltip }" class="text-right">
      <div
        v-if="
          typedVinThrow.successType === VinegarSuccessType.SUCCESS ||
          typedVinThrow.successType === VinegarSuccessType.DOUBLE_SUCCESS ||
          typedVinThrow.successType === VinegarSuccessType.DEFENDED
        "
        class="w-4"
      ></div>
      <font-awesome-icon
        v-if="
          typedVinThrow.successType === VinegarSuccessType.SHIELDED ||
          typedVinThrow.successType === VinegarSuccessType.SHIELD_DEFENDED
        "
        class="w-4"
        icon="fa-solid fa-umbrella"
      /><font-awesome-icon
        v-if="
          typedVinThrow.successType === VinegarSuccessType.DEFENDED ||
          typedVinThrow.successType === VinegarSuccessType.SHIELDED
        "
        class="w-4"
        icon="fa-solid fa-x"
      /><font-awesome-icon
        v-if="
          typedVinThrow.successType === VinegarSuccessType.SUCCESS ||
          typedVinThrow.successType === VinegarSuccessType.SHIELD_DEFENDED
        "
        class="w-4"
        icon="fa-solid fa-check"
      /><font-awesome-icon
        v-if="typedVinThrow.successType === VinegarSuccessType.DOUBLE_SUCCESS"
        class="w-4"
        icon="fa-solid fa-check-double"
      />
    </td>
  </tr>
</template>
<script setup lang="ts">
import UserLabel from "~/components/core/UserLabel.vue";
import { VinegarSuccessType, VinegarThrow } from "~/store/grapes";
import { useDateFormatter } from "~/composables/useFormatter";

// FIXME show percentage in tooltip
// FIXME show date in tooltip

const props = defineProps({
  // TODO: Fix the typing from Object to VinegarThrow in Vue Version 3.3 (currently 3.2.x)
  vinThrow: { type: Object, required: true },
});

const abbr = useLang("abbreviations");
const lang = useLang("components.vinegarThrowTable");

const typedVinThrow = computed<VinegarThrow>(
  () => props.vinThrow as VinegarThrow,
);

const timestampTimeString = computed<string>(() =>
  useDateFormatter(typedVinThrow.value.timestamp, {
    hour: "numeric",
    minute: "numeric",
    hour12: false,
  }),
);

const timestampDateString = computed<string>(() =>
  useDateFormatter(typedVinThrow.value.timestamp, {
    day: "numeric",
    month: "numeric",
  }),
);

const vinegarString = computed<string>(() =>
  useFormatter(typedVinThrow.value.vinegarThrown),
);

const successTypeTooltip = computed<string>(() =>
  lang("results." + typedVinThrow.value.successType),
);
</script>
