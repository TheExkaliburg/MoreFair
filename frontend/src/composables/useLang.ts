export const useLang = (prefix?: string) => {
  const t = (_: string) => "t";
  if (prefix !== undefined) {
    return (key: string) => t(`${prefix}.${key}`);
  } else {
    return (key: string) => t(`${key}`);
  }
};
