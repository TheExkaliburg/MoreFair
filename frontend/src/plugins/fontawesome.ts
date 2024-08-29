import { config, library } from "@fortawesome/fontawesome-svg-core";
// eslint-disable-next-line import/named
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { faDiscord } from "@fortawesome/free-brands-svg-icons";
import { defineNuxtPlugin } from "nuxt/app";
import {
  faCheck,
  faCheckDouble,
  faShieldHalved,
  faUmbrella,
  faWineBottle,
  faX,
} from "@fortawesome/free-solid-svg-icons";

// This is important, we are going to let Nuxt worry about the CSS
config.autoAddCss = false;

// You can add your icons directly in this plugin. See other examples for how you
// can add other styles or just individual icons.
library.add(faDiscord);
library.add(faShieldHalved);
library.add(faWineBottle);
library.add(faUmbrella);
library.add(faCheck);
library.add(faCheckDouble);
library.add(faX);

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.component("font-awesome-icon", FontAwesomeIcon);
});
