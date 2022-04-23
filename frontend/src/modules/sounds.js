let Sounds;
Sounds = {
  init: () => {
    Sounds.sounds = {};
  },

  register: (name, url) => {
    if (!Sounds.sounds[name]) {
      Sounds.sounds[name] = new Audio(url);
      console.warn(`Sound ${name} already registered`);
      return true;
    } else {
      return false;
    }
  },

  play: (sound) => {
    if (Sounds.sounds[sound]) {
      Sounds.sounds[sound].play();
    } else {
      console.warn(new Error(`Sound ${sound} not registered`));
    }
  },
};

export { Sounds };
