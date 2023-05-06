import { zxcvbn, zxcvbnOptions } from "@zxcvbn-ts/core";
import zxcvbnCommonPackage from "@zxcvbn-ts/language-common";
import zxcvbnEnPackage from "@zxcvbn-ts/language-en";
import { computed, Ref } from "vue";
import { FeedbackType } from "@zxcvbn-ts/core/dist/types";

export const useZxcvbn = (password: Ref<string> | string) => {
  const options = {
    translations: zxcvbnEnPackage.translations,
    graphs: zxcvbnCommonPackage.adjacencyGraphs,
    dictionary: {
      ...zxcvbnCommonPackage.dictionary,
      ...zxcvbnCommonPackage.dictionary,
    },
  };
  zxcvbnOptions.setOptions(options);

  if (typeof password === "string") {
    return computed<{
      score: number;
      feedback: FeedbackType;
      toString: string;
    }>(() => {
      const result = zxcvbn(password);
      const { score, feedback } = result;
      return { score, feedback, toString: `Score: ${score}` };
    });
  }

  return computed<{
    score: number;
    feedback: FeedbackType;
    toString: string;
  }>(() => {
    const result = zxcvbn(password.value);
    const { score, feedback } = result;
    return { score, feedback, toString: `Score: ${score}` };
  });
};
