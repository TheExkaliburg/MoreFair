const root = document.querySelector(":root");

class ThemeSelector {
  static changeTheme(name) {
    root.className = name.toLowerCase();
  }
}

export default ThemeSelector;
