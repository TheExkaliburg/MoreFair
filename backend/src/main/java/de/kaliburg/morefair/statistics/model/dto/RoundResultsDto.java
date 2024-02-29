package de.kaliburg.morefair.statistics.model.dto;

import de.kaliburg.morefair.game.round.model.RoundType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RoundResultsDto {

  @Builder.Default
  private final Map<Integer, LadderResultsDto> ladders = new HashMap<>();
  @Builder.Default
  private Set<RoundType> roundTypes = EnumSet.noneOf(RoundType.class);
  private String basePointsToPromote;
  private String createdOn;
  @Builder.Default
  private String closedOn = "stillOpen";
  private Integer number;
}
