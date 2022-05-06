const root = document.querySelector(":root");

class ThemeSelector {
  static changeTheme(name) {
    root.className = name.toLowerCase();
  }
  static getCurrentTheme() {
    let themeName = root.className;
    themeName = themeName.charAt(0).toUpperCase() + themeName.slice(1);
    themeName = themeName.split(" ")[0]; //Only first word
    return themeName;
  }
}

export default ThemeSelector;
