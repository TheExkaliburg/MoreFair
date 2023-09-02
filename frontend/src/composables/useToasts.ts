import { ToastOptions } from "vue3-toastify";
import { useNuxtApp } from "#app";

export const useToasts = (msg: string, options?: ToastOptions) => {
  const nuxtApp = useNuxtApp();
  if (msg === "" || msg === undefined) return;

  if (typeof window === "undefined") return;
  if (nuxtApp.$toast) {
    nuxtApp.$toast(msg, options);
  }
};
