// @ts-ignore
import { numberformat } from "swarm-numberformat";
import Decimal from "break_infinity.js";

export enum NumberFormatterType {
  DEFAULT,
  SMALL,
}

const defaultMinSuffix = 1e6;
const numberFormatterMap = new Map<NumberFormatterType, any>();
numberFormatterMap.set(
  NumberFormatterType.DEFAULT,
  new numberformat.Formatter({
    backend: "decimal.js",
    Decimal,
    format: "hybrid",
    sigfigs: 5,
    flavor: "short",
    minSuffix: defaultMinSuffix,
    maxSmall: 0,
    default: 0,
  }),
);
numberFormatterMap.set(
  NumberFormatterType.SMALL,
  new numberformat.Formatter({
    backend: "decimal.js",
    Decimal,
    format: "hybrid",
    sigfigs: 4,
    flavor: "short",
    minSuffix: 1e4,
    maxSmall: 0,
    default: 0,
  }),
);

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

export const useFormatter = (
  number: Decimal | number | undefined,
  type?: NumberFormatterType,
) => {
  if (number === undefined) {
    return "0";
  }
  if (typeof number === "number" && !isFinite(number)) return "∞";

  let result = numberFormatterMap
    .get(type ?? NumberFormatterType.DEFAULT)
    .format(number);

  if (result === "Infinity") result = "∞";

  return result;
};

export const useTimeFormatter = (seconds: number) => {
  if (Number.isNaN(seconds)) {
    console.error("Cannot format a NaN duration", seconds);
    return "?";
  }
  seconds = Math.ceil(seconds);
  if (seconds === Infinity) return "∞";
  if (seconds < 1) return "<1s";
  if (seconds < 60) return seconds + "s";

  const minutes = Math.floor((seconds % 3600) / 60)
    .toString()
    .padStart(2, "0");
  const remainingSeconds = (seconds % 60).toString().padStart(2, "0");
  const minutesAndSeconds = `${minutes}:${remainingSeconds}`;
  if (seconds < 3600) return minutesAndSeconds;

  const hours = Math.floor((seconds % 86400) / 3600);
  if (seconds < 86400) return `${hours}:${minutesAndSeconds}`;

  const secondsPerYear = 365 * 86400;
  const days = Math.floor((seconds % secondsPerYear) / 86400);
  const daysAndTime = `${days}d ${hours.toString().padStart(2, "0")}:${minutesAndSeconds}`;
  if (seconds < secondsPerYear) return daysAndTime;

  const years = Math.floor(seconds / secondsPerYear);
  const formattedYears = useFormatter(years);
  if (years >= defaultMinSuffix) return `${formattedYears} years`;
  return `${formattedYears}y ${days}d`;
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
