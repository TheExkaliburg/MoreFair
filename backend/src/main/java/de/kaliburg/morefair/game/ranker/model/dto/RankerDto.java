package de.kaliburg.morefair.game.ranker.model.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
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
    this.accountId = ranker.getAccountId().getId();
    this.username = ranker.getAccountId().getDisplayName();
    this.rank = ranker.getRank();
    this.points = ranker.getPoints().toString();
    this.power = ranker.getPower().toString();
    this.bias = ranker.getBias();
    this.multi = ranker.getMultiplier();
    this.isGrowing = ranker.isGrowing();
    this.assholeTag = config.getAssholeTag(ranker.getAccountId().getAssholeCount());
    this.assholePoints = ranker.getAccountId().getAssholePoints();
  }
}
