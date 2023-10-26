module.exports = {
  semi: true,
  singleQuote: false,
  plugins: ["prettier-plugin-tailwindcss"],
  tailwindConfig: "./tailwind.config.js",
  error: {
    endOfLine: "auto",
  },
};
