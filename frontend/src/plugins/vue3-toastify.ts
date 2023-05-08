import { defineNuxtPlugin } from "nuxt/app";
import Vue3Toastify, { toast, ToastContainerOptions } from "vue3-toastify";

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(Vue3Toastify, {
    autoClose: 15 * 1000,
    theme: "colored",
  } as ToastContainerOptions);
  return {
    provide: { toast },
  };
});
