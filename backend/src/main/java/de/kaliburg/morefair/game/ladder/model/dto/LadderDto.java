package de.kaliburg.morefair.game.ladder.model.dto;

import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ranker.model.dto.RankerDto;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LadderDto {

  private List<RankerDto> rankers;
  private Integer number;
  private Integer scaling;
  private Set<LadderType> types;
  private String basePointsToPromote;

}
