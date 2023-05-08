module.exports = {
  root: true,
  env: {
    browser: true,
    es6: true,
    node: true,
  },
  extends: [
    "@nuxtjs/eslint-config-typescript",
    "plugin:nuxt/recommended",
    "plugin:prettier/recommended",
  ],
  plugins: [],
  rules: {},
  overrides: [
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
      files: ["src/**/*.vue", "src/**/*.ts", "src/**/*.js"],
      rules: {
        "no-console": ["warn", { allow: ["warn", "error", "info"] }],
      },
    },
  ],
};
