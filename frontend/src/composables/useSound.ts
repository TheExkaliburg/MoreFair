import { useOptionsStore } from "~/store/options";

export const SOUNDS = {
  PROMOTION: "promotion",
  MENTION: "mention",
  GOT_FIRST: "gotFirst",
};

const audios: any = {
  promotion: new Audio("/sounds/promotion.wav"),
  mention: new Audio("/sounds/mention.wav"),
  gotFirst: new Audio("/sounds/gotFirst.wav"),
};

export const useSound = (soundName: string) => {
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
