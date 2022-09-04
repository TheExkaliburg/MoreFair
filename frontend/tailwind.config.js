module.exports = {
  theme: {
    extend: {
      colors: {
        navbar: {
          bg: "var(--navbar-bg-color)",
          text: "var(--navbar-text-color)",
        },
        button: {
          bg: {
            DEFAULT: "var(--button-bg-color)",
            hover: "var(--button-bg-hover-color)",
          },
          text: {
            DEFAULT: "var(--button-text-color)",
            hover: "var(--button-text-hover-color)",
          },
        },
        toggle: {
          bg: {
            on: "var(--toggle-bg-on-color)",
            off: "var(--toggle-bg-off-color)",
          },
          border: "var(--toggle-border-color)",
          circle: {
            bg: {
              on: "var(--toggle-circle-bg-on-color)",
              off: "var(--toggle-circle-bg-off-color)",
            },
            border: "var(--toggle-inner-border-color)",
          },
        },
      },
    },
  },
  plugins: [],
  content: ["./src/**/*.vue"],
};
