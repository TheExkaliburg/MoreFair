import { defineStore } from "pinia";
import { deepMerge } from "@antfu/utils";
import { StorageSerializers, useStorage } from "@vueuse/core";
import {
  BooleanOption,
  EditableStringListOption,
  EnumOption,
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
    showAssholePoints: new BooleanOption(true),
  }),
  ladder: new OptionsGroup({
    showEta: new BooleanOption(true),
    followOwnRanker: new BooleanOption(false),
    etaColors: new EnumOption(EtaColorType.OFF, [
      EtaColorType.OFF,
      EtaColorType.COLORS,
      EtaColorType.GRADIENT,
    ]),
    showBiasAndMulti: new BooleanOption(true),
    showPowerGain: new BooleanOption(true),
    hidePromotedPlayers: new BooleanOption(false),
    hideVinegarAndGrapes: new BooleanOption(false),
    enableSpectateAssholeLadder: new BooleanOption(false),
    hideHelpText: new BooleanOption(false),
    lockButtons: new BooleanOption(false),
  }),
  chat: new OptionsGroup({
    subscribedMentions: new EditableStringListOption(["here"]),
    playSoundOnMention: new BooleanOption(false),
    playSoundOnFirst: new BooleanOption(false),
    playSoundOnPromote: new BooleanOption(false),
    notificationVolume: new RangeOption(50, 0, 100),
  }),
  moderation: new OptionsGroup({
    enableModPage: new BooleanOption(false),
    enableChatFeatures: new BooleanOption(false),
    enableLadderFeatures: new BooleanOption(false),
    unrestrictedAccess: new BooleanOption(false),
  }),
};

export const useOptionsStore = defineStore("options", () => {
  const initialValues = {};
  Object.assign(initialValues, defaultValues);

  const state = useStorage("options", initialValues, localStorage, {
    serializer: {
      read,
      write,
    },
    mergeDefaults: (storageValue, defaultValue) =>
      deepMerge(defaultValue, storageValue),
  });

  deleteOldValues(state.value, defaultValues);

  return {
    state: state.value,
  };
});

function deleteOldValues(state: any, defaults: any) {
  Object.keys(state).forEach((key) => {
    if (!(key in defaults)) {
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
