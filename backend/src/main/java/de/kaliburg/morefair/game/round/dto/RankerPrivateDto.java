package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RankerEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RankerPrivateDto extends RankerDto {

  private String grapes;
  private String vinegar;
  private boolean autoPromote = false;

  public RankerPrivateDto(RankerEntity ranker, FairConfig config) {
    super(ranker, config);
    this.grapes = ranker.getGrapes().toString();
    this.vinegar = ranker.getVinegar().toString();
    this.autoPromote = ranker.isAutoPromote();
  }
}
