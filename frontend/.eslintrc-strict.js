module.exports = {
  root: true,
  env: {
    browser: true,
    es6: true,
    node: true,
  },
  extends: [
    "plugin:vuejs-accessibility/recommended",
  ],
  plugins: ["vuejs-accessibility"],
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
        "@typescript-eslint/no-inferrable-types": "off",
        "no-console": ["warn", { allow: ["warn", "error", "info"] }],
      },
    },
  ],
};
