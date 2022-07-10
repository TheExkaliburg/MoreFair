package de.kaliburg.morefair;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@ConfigurationProperties(prefix = "fair")
@ConfigurationPropertiesScan
@Data
public class FairConfig {

  private Integer basePeopleToPromote = 10;
  private BigInteger basePointsToPromote = new BigInteger("100000000");
  private BigInteger baseVinegarToThrow = new BigInteger("500000");
  private BigInteger baseGrapesToBuyAutoPromote = new BigInteger("5000");
  private Integer autoPromoteLadder = 1;
  private Integer manualPromoteWaitTime = 30;
  private Integer baseAssholeLadder = 10;
  private Integer baseAssholeToReset = 10;
  private List<String> assholeTags = Arrays.asList("", "♠", "♣", "♥", "♦", "♤", "♧", "♡", "♢",
      "♟", "♙", "♞", "♘", "♝", "♗", "♖", "♛", "♕", "♚", "♔", "🂠", "🂡", "🂢", "🂣", "🂣", "🂥",
      "🂦", "🂧", "🂧", "🂩", "🂪", "🂫", "🂬", "🂭", "🂮");

  public String getAssholeTag(Integer assholeCount) {
    return assholeTags.get(Math.min(assholeCount, assholeTags.size() - 1));
  }
}
