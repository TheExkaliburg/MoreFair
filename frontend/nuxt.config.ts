import { defineNuxtConfig } from "nuxt";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  css: ["virtual:windi.css", "virtual:windi-devtools"],
  modules: [
    "@pinia/nuxt",
    "@nuxtjs/partytown",
    "nuxt-windicss",
    "@vueuse/nuxt",
    "@intlify/nuxt3",
    "@nuxt/content",
  ],
  windicss: {
    analyze: true,
  },
  srcDir: "src/",
  build: {
    postcss: {
      postcssOptions: {
        plugins: {
          tailwindcss: {},
          autoprefixer: {},
        },
      },
    },
  },
  intlify: {
    localeDir: "locales",
    vueI18n: {
      locale: "en",
      fallbackLocale: "en",
      availableLocales: ["en"],
    },
  },
});
