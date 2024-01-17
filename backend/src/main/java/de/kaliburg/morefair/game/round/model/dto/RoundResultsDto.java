package de.kaliburg.morefair.game.round.model.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.dto.LadderResultsDto;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundType;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  private String closedOn = "stillOpen";
  private Integer number;

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
    createdOn = round.getCreatedOn().atZoneSameInstant(ZoneOffset.UTC).format(
        DateTimeFormatter.ISO_DATE_TIME);
    if (round.isClosed()) {
      closedOn = round.getClosedOn().atZoneSameInstant(ZoneOffset.UTC).format(
          DateTimeFormatter.ISO_DATE_TIME);
    }
    number = round.getNumber();
  }
}
