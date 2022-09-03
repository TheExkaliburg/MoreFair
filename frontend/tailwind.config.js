module.exports = {
  theme: {
    extend: {
      colors: {
        navbar: {
          bg: "var(--navbar-bg-color)",
          text: "var(--navbar-text-color)",
        },
        button: {
          bg: "var(--button-bg-color)",
          bg_hover: "var(--button-bg-hover-color)",
          text: "var(--button-text-color)",
          text_hover: "var(--button-text-hover-color)",
        },
      },
    },
  },
  plugins: [],
  content: ["./src/**/*.vue"],
};
