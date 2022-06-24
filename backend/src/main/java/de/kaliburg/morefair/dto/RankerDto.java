package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.game.round.RankerEntity;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

@Data
public class RankerDto {

  private Long accountId;
  private Integer bias;
  private boolean isYou = false;
  private Integer multiplier;
  private String points;
  private String power;
  private Integer rank;
  private String username;
  private Integer timesAsshole;
  private boolean isGrowing;

  public RankerDto(RankerEntity ranker) {
    this.accountId = ranker.getAccount().getId();
    this.username = StringEscapeUtils.unescapeJava(ranker.getAccount().getUsername());
    this.rank = ranker.getRank();
    this.points = ranker.getPoints().toString();
    this.power = ranker.getPower().toString();
    this.bias = ranker.getBias();
    this.multiplier = ranker.getMultiplier();
    this.isGrowing = ranker.isGrowing();
    this.timesAsshole = ranker.getAccount().getTimesAsshole();
  }

  public RankerDto() {

  }
}
