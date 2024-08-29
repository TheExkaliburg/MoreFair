package de.kaliburg.morefair.statistics.model.dto;

import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ranker.model.dto.RankerPrivateDto;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LadderResultsDto {

  @Builder.Default
  private final List<RankerPrivateDto> rankers = new ArrayList<>();
  @Builder.Default
  private Set<LadderType> ladderTypes = EnumSet.noneOf(LadderType.class);
  private String basePointsToPromote;
  private String createdOn;

}
