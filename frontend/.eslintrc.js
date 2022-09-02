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
  ],
};
