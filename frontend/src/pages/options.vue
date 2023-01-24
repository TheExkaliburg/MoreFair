<template>
  <div class="flex flex-row w-full justify-evenly text-text mt-12">
    <div class="flex flex-col w-64 text-xl space-y-2">
      <div class="text-3xl px-2">Settings</div>
      <button
        class="w-full px-2 text-left hover:bg-button-bg-hover hover:text-button-text-hover"
        @click="currentSection = OptionsSections.General"
      >
        General
      </button>
      <button
        class="w-full px-2 text-left hover:bg-button-bg-hover hover:text-button-text-hover"
        @click="currentSection = OptionsSections.Account"
      >
        Account
      </button>
    </div>
    <div class="flex flex-col w-reader">
      <div v-if="currentSection != OptionsSections.Account">
        <div
          v-for="option in currentOptionsArray"
          :key="currentSection + option.key"
        >
          <OptionsString
            v-if="typeof option.value === 'string'"
            :option="option"
            @update="(event) => (currentOptions[option.key] = event)"
          ></OptionsString>
          <OptionsBoolean
            v-else-if="typeof option.value === 'boolean'"
            :option="option"
            @update="(event) => (currentOptions[option.key] = event)"
          ></OptionsBoolean>
        </div>
      </div>
      <div v-else>Account Settings</div>
    </div>
    <div></div>
  </div>
</template>

<script lang="ts" setup>
import { useOptionsStore } from "~/store/options";
import OptionsString from "~/components/options/OptionsString.vue";
import OptionsBoolean from "~/components/options/OptionsBoolean.vue";

const enum OptionsSections {
  General,
  Account,
}

const currentSection = ref<OptionsSections>(OptionsSections.General);

definePageMeta({ layout: "default-without-sidebar" });

const currentOptions = computed(() => {
  switch (currentSection.value) {
    case OptionsSections.General:
      return options.state.general;
    case OptionsSections.Account:
      return options.state.account;
  }
});

const currentOptionsArray = computed(() => {
  return Object.entries(currentOptions.value).map(([key, value]) => {
    return { key, value };
  });
});

const options = useOptionsStore();
</script>

<style lang="scss"></style>
