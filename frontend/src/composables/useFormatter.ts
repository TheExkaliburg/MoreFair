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

// Set the options for the Intl.DateTimeFormat object
const dateFormatOptions: Intl.DateTimeFormatOptions = {
  weekday: "short",
  hour: "numeric",
  minute: "numeric",
  hourCycle: "h23",
};

// Create the Intl.DateTimeFormat object with the client's default locale
const dateFormatter = new Intl.DateTimeFormat(
  navigator.language,
  dateFormatOptions,
);

export const useFormatter = (number: Decimal | number) => {
  if (typeof number === "number" && !isFinite(number)) return "∞";

  let result = numberFormatter.format(number);

  if (result === "Infinity") result = "∞";

  return result;
};

export const useTimeFormatter = (seconds: number) => {
  seconds = Math.ceil(seconds);
  if (seconds === Infinity) return "∞";
  if (seconds < 1) return "<1s";
  if (seconds < 60) return seconds + "s";
  if (seconds < 3600)
    return new Date(seconds * 1000).toISOString().substring(14, 19);

  const hours = (seconds - (seconds % 3600)) / 3600;
  return hours + ":" + new Date(seconds * 1000).toISOString().substring(14, 19);
};

export const useDateFormatter = (
  utcTimestamp: number,
  options?: Intl.DateTimeFormatOptions,
) => {
  // Create a date object with the timestamp
  const date = new Date(0);
  date.setUTCSeconds(utcTimestamp);

  if (options) {
    const dateFormatter = new Intl.DateTimeFormat(navigator.language, options);
    return dateFormatter.format(date);
  }

  // Format the date using the formatter object
  return dateFormatter.format(date);
};
