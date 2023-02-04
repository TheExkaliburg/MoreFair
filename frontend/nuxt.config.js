import { defineNuxtConfig } from "nuxt/config";

export default defineNuxtConfig({
  css: [
    "virtual:windi.css",
    "virtual:windi-devtools",
    "~/assets/scss/styles.scss",
    "@fortawesome/fontawesome-svg-core/styles.css",
    "vue-virtual-scroller/dist/vue-virtual-scroller.css",
    "intro.js/minified/introjs.min.css",
  ],
  modules: [
    "@pinia/nuxt",
    "nuxt-windicss",
    "@vueuse/nuxt",
    "@nuxt/content",
    "@nuxtjs/i18n",
  ],
  srcDir: "src/",
  build: {
    transpile: [
      "@fortawesome/fontawesome-svg-core",
      "@fortawesome/free-brands-svg-icons",
      "@fortawesome/free-regular-svg-icons",
      "@fortawesome/free-solid-svg-icons",
      "@fortawesome/vue-fontawesome",
      "@headlessui/vue",
      "@heroicons/vue",
      "vue-virtual-scroller",
      "vue-i18n",
    ],
  },
  ssr: false,
  i18n: {
    locales: [
      {
        code: "en",
        file: "en.yaml",
      },
    ],
    defaultLocale: "en",
    lazy: true,
    langDir: "locales/",
    vueI18n: {
      legacy: false,
      fallbackLocale: "en",
    },
  },
});
