import { useNuxtApp } from "#app";

export const useLang = (prefix?: string) => {
  const t = useNuxtApp().$i18n.t;
  if (prefix !== undefined) {
    return (key: string) => t(`${prefix}.${key}`);
  } else {
    return (key: string) => t(`${key}`);
  }
};
