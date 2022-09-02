import { useI18n } from "vue-i18n";

export const useLang = () => {
  const { t } = useI18n();
  const lang = (key: string) => t(key);
  return lang;
};
