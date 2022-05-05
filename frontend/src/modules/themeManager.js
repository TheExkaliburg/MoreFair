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
      return rule.replace(":root.", ""); //To text
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
