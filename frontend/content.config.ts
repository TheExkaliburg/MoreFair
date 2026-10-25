import { fileURLToPath } from "node:url";
import { defineCollection, defineContentConfig } from "@nuxt/content";

// TODO (low priority): Assess lighter Markdown alternatives to Content's
// SQLite/WASM overhead, and whether Nuxt itself is justified without SSR.
// Compare payload, build complexity, maintenance benefits, and migration cost.
export default defineContentConfig({
  collections: {
    pages: defineCollection({
      type: "page",
      source: {
        cwd: fileURLToPath(new URL("./src/content/", import.meta.url)),
        include: "**/*.md",
      },
    }),
  },
});
