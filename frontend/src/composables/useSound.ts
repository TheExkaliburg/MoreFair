import { useOptionsStore } from "~/store/options";

export const SOUNDS = {
  PROMOTION: "promotion",
  MENTION: "mention",
  GOT_FIRST: "gotFirst",
};

let audios: any;
let isInitialized = false;

function initialize() {
  audios = {
    promotion: new Audio("/sounds/promotion.wav"),
    mention: new Audio("/sounds/mention.wav"),
    gotFirst: new Audio("/sounds/gotFirst.wav"),
  };
  isInitialized = true;
}

export const useSound = (soundName: string) => {
  if (!isInitialized) initialize();

  let sound: HTMLAudioElement = audios[soundName];
  if (!sound) {
    sound = audios.mention;
    soundName = "mention";
  }

  function play() {
    const optionsStore = useOptionsStore();

    const optionsKey =
      "playSoundOn" + soundName.charAt(0).toUpperCase() + soundName.slice(1);

    if (!optionsStore.state.sound[optionsKey].value) return;

    sound.volume = optionsStore.state.sound.notificationVolume.value / 100;
    return sound.play();
  }

  return { play, sound };
};
