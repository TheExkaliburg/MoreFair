package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.LadderEntity;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class RoundResultsDto {

  private final Map<Integer, LadderResultsDto> ladders = new HashMap<>();
  private Set<RoundType> roundTypes;
  private String basePointsToPromote;

  public RoundResultsDto(RoundEntity round, FairConfig config) {
    roundTypes = round.getTypes();
    basePointsToPromote = round.getBasePointsRequirement().toString();

    Map<Integer, LadderEntity> ladders = round.getLadders().stream()
        .collect(Collectors.toMap(LadderEntity::getNumber,
            Function.identity()));

    ladders.forEach((integer, ladder) -> {
      LadderResultsDto ladderResults = new LadderResultsDto(ladder, config);
      this.ladders.put(integer, ladderResults);
    });
  }
}
