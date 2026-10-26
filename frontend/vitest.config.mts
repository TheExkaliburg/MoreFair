import { resolve } from "pathe";
import { defineVitestConfig } from "@nuxt/test-utils/config";
import { configDefaults } from "vitest/config";

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
    exclude: [...configDefaults.exclude, "src/test/performance/**"],
    includeSource: ["src/**/*.{vue,ts}"],
    globals: true,
  },
});
