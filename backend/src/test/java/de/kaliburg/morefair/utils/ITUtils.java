package de.kaliburg.morefair.utils;

import com.icegreen.greenmail.spring.GreenMailBean;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

public class ITUtils {

  public static Random random = new Random();

  public static String randomIp() {
    return random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "."
        + random.nextInt(255);
  }

  public static MimeMessage getLastGreenMailMessage(GreenMailBean greenMailBean) {
    return greenMailBean.getReceivedMessages()[greenMailBean.getReceivedMessages().length - 1];
  }
}
