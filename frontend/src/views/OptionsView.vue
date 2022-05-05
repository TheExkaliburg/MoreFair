<template>
  <div class="scroller">
    <div v-for="option in options" :key="option">
      <OptionSection :options="option" />
    </div>
  </div>
</template>

<script setup>
//import CheckBox from "@/options/components/CheckBox";
//import RangeSlider from "@/options/components/RangeSlider";
//import NumberInput from "@/options/components/NumberInput";
import OptionSection from "@/options/components/OptionSection";
import { computed } from "vue";
import { getThemeNames, loadNewTheme } from "@/modules/themeManager";
import { useStore } from "vuex";
//import {
//  BoolOption,
//  RangeOption,
//  NumberOption,
//} from "@/options/entities/option";

const store = useStore();
const options = computed(() => store.state.options.options);

window.loadTheme = function (url) {
  const oldThemes = getThemeNames();
  loadNewTheme(url, () => {
    const option = store.getters["options/getOption"]("themeSelection");
    const themeNames = getThemeNames();

    //find the new theme name
    const newThemeName = themeNames.find((name) => !oldThemes.includes(name));
    //capitalize the first letter
    const newThemeNameCapitalized =
      newThemeName.charAt(0).toUpperCase() + newThemeName.slice(1);

    //insert the default theme
    themeNames.unshift("Default");
    //capitalize the first letter of each string
    let options = themeNames.map((themeName) => {
      return themeName.charAt(0).toUpperCase() + themeName.slice(1);
    });

    store.commit({
      type: "options/updateOption",
      option: option,
      payload: {
        selectedIndex: options.indexOf(newThemeNameCapitalized),
        options: options,
      },
    });
  });
};
</script>
<style lang="scss" scoped>
@import "../styles/styles";

.scroller {
  max-height: calc(100vh - 56px);
  overflow: auto !important;
}
</style>
