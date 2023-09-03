import { defineStore } from "pinia";
import axios from "axios";
import {
  BooleanOption,
  EditableMentionsOption,
  EditableThemeURLOption,
  EnumOption,
  IntegerOption,
  OptionsGroup,
  RangeOption,
} from "./entities/option";
import { useLocalStorage } from "~/composables/useLocalStorage";

export const enum EtaColorType {
  OFF = "Off",
  COLORS = "3-Colors",
  GRADIENT = "Gradient",
}

const defaultValues = {
  general: new OptionsGroup({
    showAssholePoints: new BooleanOption(false),
  }),
  ladder: new OptionsGroup({
    showEta: new BooleanOption(true),
    etaColors: new EnumOption(EtaColorType.OFF, [
      EtaColorType.OFF,
      EtaColorType.COLORS,
      EtaColorType.GRADIENT,
    ]).setIsActive(() => {
      return optionsStorage.value.ladder.showEta.value;
    }),
    biasMultiColors: new BooleanOption(true),
    showBiasAndMulti: new BooleanOption(true),
    showPowerGain: new BooleanOption(true),
    followOwnRanker: new BooleanOption(false),
    showAllRankers: new BooleanOption(false),
    showTopRankers: new IntegerOption(10).setIsActive(() => {
      return !optionsStorage.value.ladder.showAllRankers.value;
    }),
    showAboveRankers: new IntegerOption(25).setIsActive(() => {
      return !optionsStorage.value.ladder.showAllRankers.value;
    }),
    showBelowRankers: new IntegerOption(25).setIsActive(() => {
      return !optionsStorage.value.ladder.showAllRankers.value;
    }),
    showBottomRankers: new IntegerOption(10).setIsActive(() => {
      return !optionsStorage.value.ladder.showAllRankers.value;
    }),
    hidePromotedPlayers: new BooleanOption(false),
    hideZombies: new BooleanOption(false),
    hideVinegarAndGrapes: new BooleanOption(false),
    lockButtons: new BooleanOption(false),
  }),
  chat: new OptionsGroup({
    subscribedMentions: new EditableMentionsOption(["here"]),
  }),
  sound: new OptionsGroup({
    playSoundOnPromotion: new BooleanOption(false).setCallback((value) => {
      if (value) {
        useSound(SOUNDS.PROMOTION).play();
      }
    }),
    playSoundOnMention: new BooleanOption(false).setCallback((value) => {
      if (value) {
        useSound(SOUNDS.MENTION).play();
      }
    }),
    playSoundOnGotFirst: new BooleanOption(false).setCallback((value) => {
      if (value) {
        useSound(SOUNDS.GOT_FIRST).play();
      }
    }),
    notificationVolume: new RangeOption(50, 0, 100),
  }),
  theme: new OptionsGroup({
    themeSelector: new EnumOption("Default", ["Default", "Light"]).setCallback(
      (value) => {
        changeTheme(value);
      },
    ),
    themeUploader: new EditableThemeURLOption([]).setCallback(
      (value, oldValue) => {
        if (value.length < oldValue.length) {
          // figure out which index was removed from the array
          const removed = oldValue.filter((x) => !value.includes(x));
          const last = removed[removed.length - 1];
          const index = oldValue.indexOf(last) + 2;
          // remove that index from the selector array
          optionsStorage.value.theme.themeSelector.transient.options =
            optionsStorage.value.theme.themeSelector.transient.options.splice(
              index,
              1,
            );
          if (defaultValues.theme.themeSelector instanceof EnumOption)
            defaultValues.theme.themeSelector.transient.options =
              optionsStorage.value.theme.themeSelector.transient.options;

          // If that was the selected theme, change it to default
          if (
            !optionsStorage.value.theme.themeSelector.transient.options.includes(
              optionsStorage.value.theme.themeSelector.value,
            )
          ) {
            optionsStorage.value.theme.themeSelector.value = "Default";
            changeTheme("Default");
          }
        } else {
          // figure out which entry was added into the array
          const last = value[value.length - 1];
          loadTheme(last);
        }
      },
    ),
  }),
};

const optionsStorage = useLocalStorage("options", defaultValues);

export const useOptionsStore = defineStore("options", () => {
  const state = optionsStorage;
  return {
    state,
  };
});

const root = document.querySelector(":root");

function changeTheme(name: string) {
  if (root === null) return;
  root.className = name.toLowerCase();
}

function loadTheme(url: string) {
  axios.get(url).then((response) => {
    const css = response.data;
    const style = document.createElement("style");
    style.type = "text/css";
    style.innerHTML = css;
    document.getElementsByTagName("head")[0].appendChild(style);
    optionsStorage.value.theme.themeSelector.transient.options =
      getThemeNames();

    if (defaultValues.theme.themeSelector instanceof EnumOption)
      defaultValues.theme.themeSelector.transient.options =
        optionsStorage.value.theme.themeSelector.transient.options;
  });
}

function getThemeNames() {
  // find all the theme names that are like ::root.theme-name
  const themeNames = [];
  const sheets = document.styleSheets;
  for (let i = 0; i < sheets.length; i++) {
    const rules = sheets[i].cssRules;
    for (let j = 0; j < rules.length; j++) {
      const rule = rules[j];
      if (!(rule instanceof CSSStyleRule)) continue;
      if (
        rule.selectorText.startsWith(":root.") &&
        !rule.selectorText.includes(" ")
      ) {
        themeNames.push(rule.selectorText.substring(6));
      }
    }
  }
  // Capitalize the first letter
  return [
    "Default",
    ...themeNames.map((name) => name.charAt(0).toUpperCase() + name.slice(1)),
  ];
}

optionsStorage.value.theme.themeUploader.value.forEach((url: string) => {
  loadTheme(url);
});

changeTheme(optionsStorage.value.theme.themeSelector.value);
