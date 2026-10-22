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
    "@nuxt/test-utils/module",
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
        language: "en-US",
        file: "en.yml",
      },
    ],
    defaultLocale: "en",
    restructureDir: false,
    langDir: "locales/",
    vueI18n: "./i18n.config.ts",
  },
  vite: {
    build: {
      // minify: false,
    },
    test: {},
  },
});
