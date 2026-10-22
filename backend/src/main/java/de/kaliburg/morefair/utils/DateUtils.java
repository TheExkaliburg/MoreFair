package de.kaliburg.morefair.utils;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;

public class DateUtils {

  public static boolean isAprilFoolsDay() {
    return isAprilFoolsDay(LocalDate.now(ZoneOffset.UTC));
  }

  public static boolean isAprilFoolsDay(LocalDate date) {
    return date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1;
  }
}
