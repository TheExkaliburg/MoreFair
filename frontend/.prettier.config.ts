module.exports = {
  semi: true,
  singleQuote: false,
  plugins: [require("prettier-plugin-tailwindcss")],
  tailwindConfig: "./tailwind.config.js",
  error: {
    endOfLine: "auto"
  }
};
