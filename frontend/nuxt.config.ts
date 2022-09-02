import { defineNuxtConfig } from "nuxt";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  css: ["virtual:windi.css", "virtual:windi-devtools"],
  modules: [
    "@pinia/nuxt",
    "@nuxtjs/partytown",
    "nuxt-windicss",
    "@vueuse/nuxt",
  ],
  windicss: {
    analyze: true,
  },
  srcDir: "src/",
});
