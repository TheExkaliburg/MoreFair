import { defineNuxtConfig } from "nuxt";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  css: [
    "virtual:windi.css",
    "virtual:windi-devtools",
    "~/assets/scss/styles.scss",
  ],
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
    transpile: [
      "@fortawesome/fontawesome-svg-core",
      "@fortawesome/free-brands-svg-icons",
      "@fortawesome/free-regular-svg-icons",
      "@fortawesome/free-solid-svg-icons",
      "@fortawesome/vue-fontawesome",
      "@headlessui/vue",
      "@heroicons/vue",
    ],
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
