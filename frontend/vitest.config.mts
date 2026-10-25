import { resolve } from "pathe";
import { defineVitestConfig } from "@nuxt/test-utils/config";

export default defineVitestConfig({
  resolve: {
    alias: {
      "~": resolve(import.meta.dirname, "src"),
    },
  },
  test: {
    root: "./",
    environment: "nuxt",
    include: ["src/test/**/*.test.ts"],
    includeSource: ["src/**/*.{vue,ts}"],
    globals: true,
  },
});
