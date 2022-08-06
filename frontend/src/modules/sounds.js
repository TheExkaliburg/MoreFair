let Sounds = {
  setStore: (store) => {
    Sounds.store = store;
    //Initialize the sounds
    Sounds.register("gotFirstJingle", require("@/assets/gotFirstJingle.wav"));
    Sounds.register("promotionJingle", require("@/assets/promotionJingle.wav"));
    Sounds.register("mention", require("@/assets/gotFirstJingle.wav"));
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

//window.Sounds = Sounds;

export { Sounds };
