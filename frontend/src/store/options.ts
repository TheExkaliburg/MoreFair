import { defineStore } from "pinia";
import {
  BooleanOption,
  EditableStringListOption,
  EnumOption,
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
    ]),
    showBiasAndMulti: new BooleanOption(true),
    showPowerGain: new BooleanOption(true),
    followOwnRanker: new BooleanOption(false),
    hidePromotedPlayers: new BooleanOption(false),
    hideVinegarAndGrapes: new BooleanOption(false),
    lockButtons: new BooleanOption(false),
  }),
  chat: new OptionsGroup({
    subscribedMentions: new EditableStringListOption(["here"]),
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
      }
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

changeTheme(optionsStorage.value.theme.themeSelector.value);
