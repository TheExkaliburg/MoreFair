package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RankerEntity;
import lombok.Data;

@Data
public class RankerDto {

  private Long accountId;
  private String username;
  private Integer rank;
  private String points;
  private String power;
  private Integer bias;
  private Integer multi;
  private boolean isGrowing;
  private String assholeTag;
  private Integer assholePoints;

  public RankerDto(RankerEntity ranker, FairConfig config) {
    this.accountId = ranker.getAccount().getId();
    this.username = ranker.getAccount().getDisplayName();
    this.rank = ranker.getRank();
    this.points = ranker.getPoints().toString();
    this.power = ranker.getPower().toString();
    this.bias = ranker.getBias();
    this.multi = ranker.getMultiplier();
    this.isGrowing = ranker.isGrowing();
    this.assholeTag = config.getAssholeTag(ranker.getAccount().getAssholeCount());
    this.assholePoints = ranker.getAccount().getAssholePoints();
  }
}
