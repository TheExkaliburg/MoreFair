import { createConfigForNuxt } from "@nuxt/eslint-config";
import prettierRecommended from "eslint-plugin-prettier/recommended";

export default createConfigForNuxt({
  dirs: {
    root: ["."],
    src: ["./src"],
  },
  features: {
    stylistic: false,
    typescript: {
      strict: false,
    },
  },
}).append(
  {
    name: "morefair/ignores",
    ignores: [
      "**/components.d.ts",
      "**/imports.d.ts",
      "**/nuxt.d.ts",
      "**/dist/**",
      "**/node_modules/**",
      "**/.output/**",
      "**/.nuxt/**",
    ],
  },
  prettierRecommended,
  {
    name: "morefair/typescript-policy",
    files: ["**/*.{ts,tsx,mts,cts,vue}"],
    rules: {
      // Preserve the legacy preset's policy without a source-wide lint cleanup.
      "@typescript-eslint/ban-ts-comment": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/consistent-type-imports": "off",
      "@typescript-eslint/no-unused-vars": [
        "error",
        { args: "all", argsIgnorePattern: "^_" },
      ],
    },
  },
  {
    files: ["src/pages/**/*.vue", "src/layouts/**/*.vue"],
    rules: {
      "vue/multi-word-component-names": "off",
    },
  },
  {
    files: ["src/**/*.vue"],
    rules: {
      "vue/no-v-for-template-key-on-child": "error",
      "vue/no-v-for-template-key": "off",
    },
  },
  {
    files: ["src/**/*.vue", "src/**/*.ts", "src/**/*.js", "test/**/*.ts"],
    rules: {
      "no-console": ["warn", { allow: ["warn", "error", "info"] }],
    },
  },
);
