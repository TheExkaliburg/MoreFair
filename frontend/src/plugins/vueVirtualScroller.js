import { defineNuxtPlugin } from "nuxt/app";
import VueVirtualScroller from "vue-virtual-scroller";

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(VueVirtualScroller);
  // nuxtApp.vueApp.component("dynamic-scroller", DynamicScroller);
  // nuxtApp.vueApp.component("dynamic-scroller-item", DynamicScrollerItem);
});
