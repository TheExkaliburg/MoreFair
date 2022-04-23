export function secondsToHms(s) {
  if (s === 0) return "0s";
  if (!Number.isFinite(s)) return "∞";
  const negative = s < 0;
  if (negative) s = -s;
  let hours = Math.floor(s / 3600);
  let minutes = Math.floor((s % 3600) / 60);
  let seconds = (s % 3600) % 60;

  if (hours == 0 && minutes == 0 && seconds !== 0) {
    if (Math.abs(seconds) < 1) {
      return "<1s";
    }
    return `${negative ? "-" : ""}${Math.floor(seconds)}s`;
  }

  let mayPadd = false;
  const padd = (num, suff) => {
    if (num == 0 && !mayPadd) return "";
    if (num < 10 && mayPadd) {
      return "0" + num + suff;
    }
    mayPadd = true;
    return num + suff;
  };

  seconds = Math.floor(seconds);

  return (
    (negative ? "-" : "") +
    padd(hours, ":", false) +
    padd(minutes, ":") +
    padd(seconds, "")
  );
}
