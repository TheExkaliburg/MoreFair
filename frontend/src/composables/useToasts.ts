import { useNuxtApp } from "#app";
import { ToastOptions } from "vue3-toastify";

export const useToasts = (msg: string, options?: ToastOptions) => {
  const nuxtApp = useNuxtApp();
  if (msg === "" || msg === undefined) return;

  if (nuxtApp.$toast) {
    nuxtApp.$toast(msg, options);
  }
};
