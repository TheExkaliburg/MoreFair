package de.kaliburg.morefair.game.ranker.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RankerPrivateDto extends RankerDto {

  private String grapes;
  private String vinegar;
  private boolean autoPromote;
}
