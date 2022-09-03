import { useI18n } from "vue-i18n";

export const useLang = (prefix?: string) => {
  const { t } = useI18n();
  if (prefix !== undefined) {
    return (key: string) => t(`${prefix}.${key}`);
  } else {
    return (key: string) => t(`${key}`);
  }
};
