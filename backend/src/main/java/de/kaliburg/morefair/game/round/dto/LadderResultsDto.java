package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.LadderEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class LadderResultsDto {

  private Map<Integer, List<RankerPrivateDto>> allLadders = new HashMap<>();

  public LadderResultsDto(Map<Integer, LadderEntity> ladders, FairConfig config) {
    ladders.forEach((integer, ladder) -> {
      List<RankerPrivateDto> allRankers = new ArrayList<>();
      ladder.getRankers().forEach(ranker -> {
        RankerPrivateDto dto = new RankerPrivateDto(ranker, config);
        allRankers.add(dto);
      });

      allLadders.put(integer, allRankers);
    });
  }
}
