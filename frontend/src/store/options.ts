import { defineStore } from "pinia";
import { deepMerge } from "@antfu/utils";
import { StorageSerializers, useStorage } from "@vueuse/core";
import {
  BooleanOption,
  EditableStringListOption,
  EnumOption,
  ObjectFunctions,
  OptionsGroup,
  RangeOption,
} from "./entities/option";

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
    // followOwnRanker: new BooleanOption(false),
    etaColors: new EnumOption(EtaColorType.OFF, [
      EtaColorType.OFF,
      EtaColorType.COLORS,
      EtaColorType.GRADIENT,
    ]),
    showBiasAndMulti: new BooleanOption(true),
    showPowerGain: new BooleanOption(true),
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
};

export const useOptionsStore = defineStore("options", () => {
  const initialValues = {};
  Object.assign(initialValues, defaultValues);
  const cloneOfInitialValue = JSON.parse(JSON.stringify(initialValues));

  const state = useStorage("options", initialValues, localStorage, {
    serializer: {
      read,
      write,
    },
    mergeDefaults: (storageValue, defaultValue) =>
      deepMerge(defaultValue, storageValue),
  });

  deleteOldValues(state.value, cloneOfInitialValue);

  return {
    state: state.value,
  };
});

function deleteOldValues(state: any, defaults: any) {
  Object.keys(state).forEach((key) => {
    if (!(key in defaults) && !ObjectFunctions.includes(key)) {
      delete state[key];
    } else if (typeof state[key] === "object") {
      deleteOldValues(state[key], defaults[key]);
    }
  });
}

function deleteEntriesWithKey(state: any, keys: string[]) {
  Object.keys(state).forEach((key) => {
    if (keys.includes(key)) {
      delete state[key];
    } else if (typeof state[key] === "object") {
      deleteEntriesWithKey(state[key], keys);
    }
  });
}

function read(value: any) {
  return StorageSerializers.object.read(value);
}

function write(value: any) {
  const temp = JSON.parse(JSON.stringify(value));
  deleteEntriesWithKey(temp, ["transient"]);
  return StorageSerializers.object.write(temp);
}
