package de.kaliburg.morefair.utils;

import java.util.Random;

public class ITUtils {

  public static Random random = new Random();


  public static String randomIp() {
    return random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "."
        + random.nextInt(255);
  }
}
