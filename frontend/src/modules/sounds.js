let Sounds = {
  setStore: (store) => {
    Sounds.store = store;
  },
  register: (name, url) => {
    Sounds.store.commit({
      type: "sounds/loadSound",
      name: name,
      url: url,
    });
  },

  play: (sound, volume = 100) => {
    const sounds = Sounds.store.getters["sounds/getSound"];
    const soundObj = sounds(sound);
    if (soundObj) {
      soundObj.volume = volume / 100;
      soundObj.play();
    } else {
      console.warn(new Error(`Sound ${sound} not registered`));
    }
  },
};

window.Sounds = Sounds;

export { Sounds };
