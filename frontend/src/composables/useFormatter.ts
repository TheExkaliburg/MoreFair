// @ts-ignore
import { numberformat } from "swarm-numberformat";
import Decimal from "break_infinity.js";

const numberFormatter = new numberformat.Formatter({
  backend: "decimal.js",
  Decimal,
  format: "hybrid",
  sigfigs: 5,
  flavor: "short",
  minSuffix: 1e6,
  maxSmall: 0,
  default: 0,
});

export const useFormatter = (number: Decimal | number) => {
  if (typeof number === "number" && !isFinite(number)) return "∞";

  return numberFormatter.format(number);
};
