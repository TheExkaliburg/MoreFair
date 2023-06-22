module.exports = {
  semi: true,
  singleQuote: false,
  plugins: [require("prettier-plugin-tailwind")],
  tailwindConfig: "./tailwind.config.js",
  error: {
    endOfLine: "auto",
  },
};
