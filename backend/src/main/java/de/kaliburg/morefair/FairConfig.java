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

  private BigInteger basePointsToPromote = BigInteger.valueOf(100_000_000L);
  private BigInteger baseVinegarToThrow = BigInteger.valueOf(500_000L);
  private BigInteger baseGrapesToBuyAutoPromote = BigInteger.valueOf(2_000L);
  private Integer autoPromoteLadder = 1;
  private Integer manualPromoteWaitTime = 30;
  private Integer minimumPeopleForPromote = 10;
  private Secrets secrets;
  private Integer minVinegarPercentageThrown = 50;
  private Integer maxVinegarThrown = 100;

  private List<String> assholeTags = Arrays.asList("", "♣", "♠", "♦", "♥", "♙", "♘", "♗", "♖", "♕",
      "♔", "🂠", "🂱", "🂲", "🂳", "🂴", "🂵", "🂶", "🂷", "🂸", "🂹", "🂺", "🂻", "🂽", "🂾");

  public String getAssholeTag(Integer assholeLevel) {
    return assholeTags.get(Math.min(assholeLevel, assholeTags.size() - 1));
  }

  /**
   * Calculates the maximum assholePoints necessary to get the highest asshole tag.
   *
   * @return the maximum assholePoints
   */
  public Integer getMaxAssholePointsAsTag() {
    // 1  10
    // 2  30
    // 3  60
    // 4 100
    // etc
    int size = assholeTags.size() - 1;
    int result = (size * (size + 1)) / 2;
    return result * 10;
  }

  @Data
  public static class Secrets {

    private String rememberMe;
  }
}