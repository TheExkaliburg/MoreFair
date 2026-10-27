import { defineNuxtConfig } from "nuxt/config";

export default defineNuxtConfig({
  compatibilityDate: "2024-04-03",
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
  // TODO (low priority): Evaluate adopting Nuxt's default app/ structure
  // to reduce path configuration, separately from dependency modernization.
  srcDir: "src/",
  dir: {
    public: "src/public",
  },
  content: {
    experimental: {
      sqliteConnector: "better-sqlite3",
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
  typescript: {
    nodeTsConfig: {
      include: [
        "../test/e2e.test.ts",
        "../vitest*.mts",
        "../content.config.ts",
      ],
    },
    tsConfig: {
      include: [
        "../test/unit/**/*.ts",
        "../test/helpers/**/*.ts",
        "../test/performance/**/*.ts",
      ],
      compilerOptions: {
        verbatimModuleSyntax: false,
      },
    },
  },
  i18n: {
    locales: [
      {
        code: "en",
        language: "en-US",
        file: "en.yml",
      },
    ],
    defaultLocale: "en",
    restructureDir: ".",
    langDir: "src/locales/",
    vueI18n: "./i18n.config.ts",
  },
  vite: {
    build: {
      // minify: false,
    },
    test: {},
  },
});
