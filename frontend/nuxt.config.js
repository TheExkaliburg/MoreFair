import { defineNuxtConfig } from "nuxt/config";

export default defineNuxtConfig({
  css: [
    "~/assets/scss/styles.scss",
    "@fortawesome/fontawesome-svg-core/styles.css",
    "vue-virtual-scroller/dist/vue-virtual-scroller.css",
    "intro.js/minified/introjs.min.css",
    "vue3-toastify/dist/index.css",
    "tippy.js/dist/tippy.css",
  ],
  modules: [
    "nuxt-windicss",
    "@pinia/nuxt",
    "@vueuse/nuxt",
    "@nuxt/content",
    "@nuxtjs/i18n",
    "nuxt-vitest",
  ],
  srcDir: "src/",
  content: {
    experimental: {
      clientDB: true,
    },
  },
  build: {
    transpile: [
      "@fortawesome/fontawesome-svg-core",
      "@fortawesome/free-brands-svg-icons",
      "@fortawesome/free-regular-svg-icons",
      "@fortawesome/free-solid-svg-icons",
      "@fortawesome/vue-fontawesome",
      "@headlessui/vue",
      "vue-virtual-scroller",
      "vue-i18n",
    ],
  },
  ssr: false,
  i18n: {
    locales: [
      {
        code: "en",
        iso: "en-US",
        file: "en.yml",
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
  vite: {
    build: {
      // minify: false,
    },
    test: {},
  },
});
