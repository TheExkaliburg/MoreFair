import { useNuxtApp } from "#app";

export const useLang = (prefix?: string) => {
  if (prefix !== undefined) {
    return (key: string) => ct(`${prefix}.${key}`);
  } else {
    return (key: string) => ct(`${key}`);
  }
};

function ct(key: string) {
  const t = useNuxtApp().$i18n.t;

  let result = t(key);
  let i = 0;
  while (result.includes("\\n") && i < 100) {
    result = result.replaceAll("\\n", "<br/>");
    i++;
  }

  return result;
}
