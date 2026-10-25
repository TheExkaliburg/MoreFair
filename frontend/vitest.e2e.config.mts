import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["test/e2e.test.ts"],
    environment: "node",
    testTimeout: 30000,
    hookTimeout: 30000,
  },
});
