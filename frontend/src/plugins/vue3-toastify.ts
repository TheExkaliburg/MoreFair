import { defineNuxtPlugin } from "nuxt/app";
import Vue3Toastify, { toast, ToastContainerOptions } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(Vue3Toastify, {
    autoClose: false,
  } as ToastContainerOptions);
  return {
    provide: { toast },
  };
});
