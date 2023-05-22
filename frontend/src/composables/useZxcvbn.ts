import { zxcvbnAsync, zxcvbnOptions } from "@zxcvbn-ts/core";
import { OptionsType } from "@zxcvbn-ts/core/dist/types";

const lang = useLang("zxcvbn");
let isInitialized = false;

async function setOptions() {
  const common = (await import("@zxcvbn-ts/language-common")).default;
  const en = (await import("@zxcvbn-ts/language-en")).default;
  const options: OptionsType = {
    dictionary: {
      ...common.dictionary,
      ...en.dictionary,
    },
    graphs: common.adjacencyGraphs,
    useLevenshteinDistance: true,
    translations: en.translations,
  };
  zxcvbnOptions.setOptions(options);
  isInitialized = true;
}

export const useZxcvbn = async (password: string) => {
  if (password.length > 0 && !isInitialized) await setOptions();

  const result = await zxcvbnAsync(password);
  const { score, feedback } = result;
  return { score, feedback, toString: `${lang("strength")}: ${score}` };
};
