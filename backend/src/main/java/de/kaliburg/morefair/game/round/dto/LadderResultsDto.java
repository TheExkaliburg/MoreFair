package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.LadderEntity;
import de.kaliburg.morefair.game.round.LadderType;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class LadderResultsDto {

  private final List<RankerPrivateDto> rankers = new ArrayList<>();
  private Set<LadderType> ladderTypes;
  private String basePointsToPromote;

  private String createdOn;

  public LadderResultsDto(LadderEntity ladder, FairConfig config) {
    basePointsToPromote = ladder.getBasePointsToPromote().toString();
    ladderTypes = ladder.getTypes();
    ladder.getRankers().forEach(ranker -> {
      RankerPrivateDto dto = new RankerPrivateDto(ranker, config);
      rankers.add(dto);
    });
    createdOn = ladder.getCreatedOn().atZoneSameInstant(ZoneOffset.UTC).format(
        DateTimeFormatter.ISO_DATE_TIME);
  }
}
