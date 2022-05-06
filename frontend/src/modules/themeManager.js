import store from "../store";

function getCSSRuleList(ruleName) {
  let styleSheets = document.styleSheets;
  let cssRules = [];
  for (let i = 0; i < styleSheets.length; i++) {
    let styleSheet = styleSheets[i];
    if (!styleSheet.cssRules) {
      continue;
    }
    let rules = styleSheet.cssRules;
    for (let j = 0; j < rules.length; j++) {
      let rule = rules[j];
      if (rule.selectorText && rule.selectorText.startsWith(ruleName)) {
        cssRules.push(rule);
      }
    }
  }
  return cssRules;
}

function getCSSRulesNonVue(ruleName) {
  let cssRules = getCSSRuleList(ruleName);
  return cssRules.filter((rule) => {
    return rule.selectorText && rule.selectorText.indexOf("[data-v-") === -1;
  });
}

function getCSSUniqueRuleNames(ruleName) {
  let cssRules = getCSSRulesNonVue(ruleName);
  return cssRules
    .map((rule) => {
      return rule.selectorText; //To text
    })
    .filter((rule, index, self) => {
      return self.indexOf(rule) === index; //Unique
    });
}

export function getThemeNames() {
  let cssRules = getCSSUniqueRuleNames(":root");
  return cssRules
    .map((rule) => {
      return rule.replace(":root.", "");
    })
    .map((rule) => {
      return rule.split(" ")[0]; //Only first word
    })
    .filter((rule, index, self) => {
      return self.indexOf(rule) === index; //Unique
    })
    .filter((rule) => {
      return rule !== ":root";
    });
}

export function loadNewTheme(url, callback) {
  //load the url into a variable
  let xhr = new XMLHttpRequest();
  xhr.open("GET", url, true);
  xhr.onload = function () {
    if (this.status >= 200 && this.status < 400) {
      // Success!
      let css = this.responseText;
      //replace the css
      let style = document.createElement("style");
      style.type = "text/css";
      style.innerHTML = css;
      document.getElementsByTagName("head")[0].appendChild(style);
      callback();
    }
  };
  xhr.send();
}

export function exposeThemeManagerToConsole() {
  window.loadTheme = function (url) {
    const oldThemes = getThemeNames();
    loadNewTheme(url, () => {
      const option = store.getters["options/getOption"]("themeSelection");
      const themeNames = getThemeNames();

      //find the new theme name
      const newThemeName = themeNames.find((name) => !oldThemes.includes(name));
      //capitalize the first letter
      const newThemeNameCapitalized =
        newThemeName.charAt(0).toUpperCase() + newThemeName.slice(1);

      //insert the default theme
      themeNames.unshift("Default");
      //capitalize the first letter of each string
      let options = themeNames.map((themeName) => {
        return themeName.charAt(0).toUpperCase() + themeName.slice(1);
      });

      store.commit({
        type: "options/updateOption",
        option: option,
        payload: {
          selectedIndex: options.indexOf(newThemeNameCapitalized),
          options: options,
        },
      });
    });
  };
}
