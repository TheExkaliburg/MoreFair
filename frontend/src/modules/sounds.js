import store from "../store";

const sounds = store.getters["sounds/getSound"];

let Sounds;
Sounds = {
  register: (name, url) => {
    store.commit({
      type: "sounds/loadSound",
      name: name,
      url: url,
    });
  },

  play: (sound, volume = 100) => {
    const soundObj = sounds(sound);
    if (soundObj) {
      soundObj.volume = volume / 100;
      soundObj.play();
    } else {
      console.warn(new Error(`Sound ${sound} not registered`));
    }
  },
};

export { Sounds };
