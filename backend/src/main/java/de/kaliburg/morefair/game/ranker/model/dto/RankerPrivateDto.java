package de.kaliburg.morefair.game.ranker.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class RankerPrivateDto extends RankerDto {

  private String grapes;
  private String vinegar;
  private String wine;
  private boolean autoPromote;
}
