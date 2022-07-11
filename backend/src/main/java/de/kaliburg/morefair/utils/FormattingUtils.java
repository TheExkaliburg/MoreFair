package de.kaliburg.morefair.utils;

public class FormattingUtils {

  /**
   * Return the ordinal of a cardinal number (positive integer) (as per common usage rather than set
   * theory).
   *
   * @param i the number that needs to be formatted
   * @return the String of i in ordinal form
   * @throws IllegalArgumentException if i is less than 0.
   */
  public static String ordinal(int i) {
    if (i < 0) {
      throw new IllegalArgumentException(
          "Only positive integers (cardinals) have an ordinal but " + i + " was supplied");
    }

    String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
    return switch (i % 100) {
      case 11, 12, 13 -> i + "th";
      default -> i + suffixes[i % 10];
    };
  }
}