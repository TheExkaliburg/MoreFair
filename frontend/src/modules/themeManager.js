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

export function loadTheme(url, onlyLoad = false, callback = () => {}) {
  const oldThemes = getThemeNames();
  loadNewTheme(url, () => {
    const option = store.getters["options/getOption"]("themeSelection");
    const themeNames = getThemeNames();

    //find the new theme name
    const newThemeName = themeNames.find((name) => !oldThemes.includes(name));
    let newThemeNameCapitalized = "";
    try {
      //capitalize the first letter
      newThemeNameCapitalized =
        newThemeName.charAt(0).toUpperCase() + newThemeName.slice(1);
    } catch (e) {
      deleteThemeURL(url, oldThemes); //This url is not a valid theme.
      console.log("Invalid theme " + url);
      return;
    }

    //insert the default theme
    themeNames.unshift("Default");
    //capitalize the first letter of each string
    let options = themeNames.map((themeName) => {
      return themeName.charAt(0).toUpperCase() + themeName.slice(1);
    });

    saveTheme(newThemeNameCapitalized, url);

    if (!onlyLoad) {
      store.commit({
        type: "options/updateOption",
        option: option,
        payload: {
          selectedIndex: options.indexOf(newThemeNameCapitalized),
          options: options,
        },
      });
    } else {
      store.commit({
        type: "options/updateOption",
        option: option,
        payload: {
          options: options,
        },
      });
    }
    callback();
  });
}

function deleteThemeURL(url, preventDelete = []) {
  const themeDatabase = localStorage.getItem("themeDatabase");
  if (!themeDatabase) {
    return;
  }
  const themeDatabaseObject = JSON.parse(themeDatabase);
  Object.keys(themeDatabaseObject).forEach((key) => {
    if (themeDatabaseObject[key] === url && !preventDelete.includes(key)) {
      delete themeDatabaseObject[key];
    }
  });
}

export function deleteNamedTheme(themeName) {
  console.log("Deleting theme: " + themeName);
  const themeDatabase = localStorage.getItem("themeDatabase");
  if (!themeDatabase) {
    return;
  }
  const themeDatabaseObject = JSON.parse(themeDatabase);
  delete themeDatabaseObject[themeName];
  localStorage.setItem("themeDatabase", JSON.stringify(themeDatabaseObject));
}

function saveTheme(themeName, themeUrl) {
  let themeDatabase = localStorage.getItem("themeDatabase");
  if (!themeDatabase) {
    themeDatabase = JSON.stringify({});
  }
  const themeDatabaseObject = JSON.parse(themeDatabase);
  themeDatabaseObject[themeName] = themeUrl;
  localStorage.setItem("themeDatabase", JSON.stringify(themeDatabaseObject));
}

export function requestTheme(themeName) {
  const themeDatabase = localStorage.getItem("themeDatabase");
  if (!themeDatabase) {
    return;
  }
  const themeDatabaseObject = JSON.parse(themeDatabase);
  const theme = themeDatabaseObject[themeName];
  if (!theme) {
    return;
  }
  loadTheme(theme);
}

export function requestAllThemes(callback = () => {}) {
  const themeDatabase = localStorage.getItem("themeDatabase");
  if (!themeDatabase) {
    return;
  }
  const themeDatabaseObject = JSON.parse(themeDatabase);
  let awaitingLoadedCount = 1;
  Object.values(themeDatabaseObject).forEach((theme) => {
    awaitingLoadedCount++;
    loadTheme(theme, true, () => {
      awaitingLoadedCount--;
      if (awaitingLoadedCount === 0) callback();
    });
  });
  awaitingLoadedCount--;
  if (awaitingLoadedCount === 0) {
    callback();
  }
}

export function exposeThemeManagerToConsole() {
  window.loadTheme = loadTheme;
}
