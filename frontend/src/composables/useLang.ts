import { useNuxtApp } from "#app";

export const useLang = (prefix?: string) => {
  const t = useNuxtApp().$i18n.t;
  if (prefix !== undefined) {
    return (key: string) => ct(`${prefix}.${key}`);
  } else {
    return (key: string) => ct(`${key}`);
  }

  function ct(key: string) {
    let result = t(key);

    let i = 0;
    while (result.includes("\\n") && i < 100) {
      result = result.replaceAll("\\n", "<br/>");
      i++;
    }

    return result;
  }
};
