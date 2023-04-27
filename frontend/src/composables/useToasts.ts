import { useNuxtApp } from "#app";
import { ToastOptions } from "vue3-toastify";

export const useToasts = (msg: string, options?: ToastOptions) => {
  const nuxtApp = useNuxtApp();
  return nuxtApp.$toast(msg, options);
};
