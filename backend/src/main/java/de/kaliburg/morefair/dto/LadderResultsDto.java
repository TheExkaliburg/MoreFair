package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.game.round.LadderEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class LadderResultsDto {

  private Map<Integer, List<RankerPrivateDto>> allLadders = new HashMap<>();

  public LadderResultsDto(Map<Integer, LadderEntity> ladders) {
    ladders.forEach((integer, ladder) -> {
      List<RankerPrivateDto> allRankers = new ArrayList<>();
      ladder.getRankers().forEach(ranker -> {
        RankerPrivateDto dto = ranker.convertToPrivateDto();
        allRankers.add(dto);
      });

      allLadders.put(integer, allRankers);
    });
  }
}
