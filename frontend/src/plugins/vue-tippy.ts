import { defineNuxtPlugin } from "nuxt/app";
import VueTippy from "vue-tippy";

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(VueTippy, {
    directive: "tippy",
    component: "tippy",
  });
});
