package de.kaliburg.morefair.game.ranker.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
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

}
