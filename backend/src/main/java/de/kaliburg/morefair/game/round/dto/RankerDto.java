package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RankerEntity;
import lombok.Data;

@Data
public class RankerDto {

  private Long accountId;
  private Integer bias;
  private boolean isYou = false;
  private Integer multi;
  private String points;
  private String power;
  private Integer rank;
  private String username;
  private String tag;
  private boolean isGrowing;

  public RankerDto(RankerEntity ranker, FairConfig config) {
    this.accountId = ranker.getAccount().getId();
    this.username = ranker.getAccount().getUsername();
    this.rank = ranker.getRank();
    this.points = ranker.getPoints().toString();
    this.power = ranker.getPower().toString();
    this.bias = ranker.getBias();
    this.multi = ranker.getMultiplier();
    this.isGrowing = ranker.isGrowing();
    this.tag = config.getAssholeTag(ranker.getAccount().getAssholeCount());
  }
}
