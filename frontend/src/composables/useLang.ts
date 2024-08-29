import { useNuxtApp } from "#app";

const NEW_LINE_PLACEHOLDER = "\\n";
const NEW_LINE_REPLACEMENT = "<br/>";
const STRING_PLACEHOLDER = "%s";

export const useLang = (prefix?: string) => {
  if (prefix !== undefined) {
    return (key: string, ...args: string[]) => ct(`${prefix}.${key}`, ...args);
  } else {
    return (key: string, ...args: string[]) => ct(`${key}`, ...args);
  }
};

function ct(key: string, ...args: string[]) {
  const t = useNuxtApp().$i18n.t;

  let result = t(key);
  let i = 0;
  while (result.includes(NEW_LINE_PLACEHOLDER) && i < 100) {
    result = result.replaceAll(NEW_LINE_PLACEHOLDER, NEW_LINE_REPLACEMENT);
    i++;
  }

  i = 0;
  while (result.includes(STRING_PLACEHOLDER) && i < args.length) {
    result = result.replace(STRING_PLACEHOLDER, args[i]);
    i++;
  }

  return result;
}
