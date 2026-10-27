// @vitest-environment happy-dom
import { describe, expect, test, vi } from "vitest";
import { useTimeFormatter } from "~/composables/useFormatter";

describe("duration formatting", () => {
  test.each([
    [-1, "<1s"],
    [0, "<1s"],
    [0.1, "1s"],
    [59, "59s"],
    [59.1, "01:00"],
    [60, "01:00"],
    [3599, "59:59"],
    [3599.1, "1:00:00"],
    [3600, "1:00:00"],
    [86399, "23:59:59"],
    [86399.1, "1d 00:00:00"],
    [86400, "1d 00:00:00"],
    [90061, "1d 01:01:01"],
    [183845, "2d 03:04:05"],
    [31_535_999, "364d 23:59:59"],
    [31_535_999.1, "1y 0d"],
    [31_536_000, "1y 0d"],
    [31_622_400, "1y 1d"],
    [63_345_906, "2y 3d"],
    [Infinity, "∞"],
    [-Infinity, "<1s"],
    [31_536_000 * 999_999 + 86400, "999,999y 1d"],
    [31_536_000 * 1e6, "1.0000M years"],
    [31_536_000 * 1e6 + 86400, "1.0000M years"],
    [31_536_000 * 1e9, "1.0000B years"],
    [8_640_000_000_001, "273,972y 220d"],
    [1e15, "31.710M years"],
  ])("formats %s seconds as %s", (seconds, expected) => {
    expect(useTimeFormatter(seconds)).toBe(expected);
  });

  test("formats finite durations even when milliseconds would overflow", () => {
    const formatted = useTimeFormatter(Number.MAX_VALUE);
    expect(formatted).not.toMatch(/NaN|Infinity/);
    expect(formatted).toMatch(/^\d.* years$/);
  });

  test("reports invalid durations and displays an unknown marker", () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      expect(useTimeFormatter(NaN)).toBe("?");
      expect(error).toHaveBeenCalledExactlyOnceWith(
        "Cannot format a NaN duration",
        NaN,
      );
    } finally {
      error.mockRestore();
    }
  });
});
