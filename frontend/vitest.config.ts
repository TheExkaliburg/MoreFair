import { resolve } from "pathe";
import { defineVitestConfig } from "nuxt-vitest/config";

export default defineVitestConfig({
  resolve: {
    alias: {
      "~": resolve(__dirname, "src"),
    },
  },
  test: {
    root: "./",
    environment: "nuxt",
    includeSource: ["src/**/*.{vue,ts}"],
    globals: true,
  },
});
