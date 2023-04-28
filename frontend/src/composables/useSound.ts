const audios: any = {
  promotion: new Audio("/sounds/promotion.wav"),
  mention: new Audio("/sounds/mention.wav"),
  gotFirst: new Audio("/sounds/gotFirst.wav"),
};

export const useSound = (soundName: string) => {
  let sound: HTMLAudioElement;
  if (!audios[soundName]) {
    sound = new Audio(`/sounds/${soundName}.mp3`);
    audios[soundName] = sound;
  } else {
    sound = audios[soundName];
  }

  return sound;
};
